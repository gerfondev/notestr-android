#!/usr/bin/env python3
"""Rebuild the pinned Nostr FFI with its audited lockfile; never publish anything.

Requires Python 3.12+, Rust/rustup and the Android NDK recorded in provenance.json.
--package-only verifies and packages an already completed build.
"""
import argparse
import ctypes
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
VENDOR = ROOT / 'vendor' / 'nostr-sdk'
PROVENANCE = json.loads((VENDOR / 'provenance.json').read_text())
TARGETS = {
    'x86_64-linux-android': ('x86_64', 'x86_64-linux-android'),
    'aarch64-linux-android': ('arm64-v8a', 'aarch64-linux-android'),
    'armv7-linux-androideabi': ('armeabi-v7a', 'armv7a-linux-androideabi'),
    'i686-linux-android': ('x86', 'i686-linux-android'),
}


def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def checked_download(url, expected, target):
    if not target.exists():
        with urllib.request.urlopen(url, timeout=90) as response, target.open('wb') as output:
            shutil.copyfileobj(response, output)
    if sha(target) != expected:
        raise ValueError(f'Checksum mismatch: {target.name}')
    return target


def check_source(source):
    archive = VENDOR / 'upstream-source.tar.gz'
    checked_download(PROVENANCE['upstreamSourceUrl'], PROVENANCE['upstreamSourceSha256'], archive)
    if not source.exists():
        source.mkdir(parents=True)
        with tarfile.open(archive) as tar:
            # Only extract relative regular files/directories from the known source archive.
            for member in tar.getmembers():
                relative = Path(*Path(member.name).parts[1:])
                if not relative.parts:
                    continue
                if relative.is_absolute() or '..' in relative.parts or not (member.isfile() or member.isdir()):
                    raise ValueError('Unsafe source archive entry')
                target = source / relative
                if member.isdir():
                    target.mkdir(parents=True, exist_ok=True)
                else:
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(tar.extractfile(member).read())
        for name in ['Cargo.toml', 'Cargo.lock']:
            shutil.copyfile(VENDOR / name, source / name)
    with tarfile.open(archive) as tar:
        for member in tar.getmembers():
            relative = Path(*Path(member.name).parts[1:])
            if member.isfile() and str(relative) not in ['Cargo.toml', 'Cargo.lock']:
                if (source / relative).read_bytes() != tar.extractfile(member).read():
                    raise ValueError(f'Modified upstream source: {relative}')
    for name in ['Cargo.toml', 'Cargo.lock']:
        assert (source / name).read_bytes() == (VENDOR / name).read_bytes(), name
    assert sha(source / 'Cargo.lock') == PROVENANCE['cargoLockSha256']


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-dir', type=Path, default=Path('/tmp/notestr-sdk-rebuild'))
    parser.add_argument('--ndk', type=Path, required=True)
    parser.add_argument('--package-only', action='store_true')
    parser.add_argument('--official-aar', type=Path)
    parser.add_argument('--official-sources', type=Path)
    args = parser.parse_args()
    source, ndk = args.source_dir.resolve(), args.ndk.resolve()
    check_source(source)
    assert re.search(r'Pkg.Revision\s*=\s*' + re.escape(PROVENANCE['androidNdk']) + r'\s*$',
                     (ndk / 'source.properties').read_text(), re.M)
    toolbin = ndk / 'toolchains/llvm/prebuilt/linux-x86_64/bin'
    env = dict(os.environ, RUSTUP_TOOLCHAIN=PROVENANCE['rustToolchain'], CARGO_BUILD_JOBS='4')
    flags = f'--remap-path-prefix={Path.home()}=/build --remap-path-prefix={source}=/build/notestr-sdk --remap-path-prefix={ndk.parent}=/build/toolchain'
    env['RUSTFLAGS'] = flags
    if not args.package_only:
        subprocess.run(['cargo', 'build', '--locked', '--lib'], cwd=source, env=env, check=True)
        env['RUSTFLAGS'] += ' -Clink-arg=-Wl,-z,max-page-size=16384'
        for target, (_, compiler) in TARGETS.items():
            cc = str(toolbin / f'{compiler}{PROVENANCE["androidMinApi"]}-clang')
            env['CARGO_TARGET_' + target.upper().replace('-', '_') + '_LINKER'] = cc
            env['CC_' + target.replace('-', '_')] = cc
            env['CC_' + target] = cc
            env['AR_' + target.replace('-', '_')] = str(toolbin / 'llvm-ar')
        for target in TARGETS:
            subprocess.run(['cargo', 'build', '--locked', '--release', '--lib', '--target', target],
                           cwd=source, env=env, check=True)
    aar = checked_download(PROVENANCE['officialAarUrl'], PROVENANCE['officialAarSha256'],
                           args.official_aar or source / 'official.aar')
    sources = checked_download(PROVENANCE['officialSourcesUrl'], PROVENANCE['officialSourcesSha256'],
                               args.official_sources or source / 'official-sources.jar')
    with zipfile.ZipFile(sources) as archive:
        binding = archive.read('org/nostrdevkit/sdk/nostr_sdk.kt').decode()
    host = ctypes.CDLL(str(source / 'target/debug/libnostr_sdk_ffi.so'))
    expected_contract = int(re.search(r'val bindings_contract_version = (\d+)', binding)[1])
    host.ffi_nostr_sdk_ffi_uniffi_contract_version.restype = ctypes.c_uint32
    assert host.ffi_nostr_sdk_ffi_uniffi_contract_version() == expected_contract
    checks = re.findall(r'if \(lib\.(\w+checksum\w+)\(\) != (\d+)\)', binding)
    assert len(checks) == 522
    for name, expected in checks:
        fn = getattr(host, name)
        fn.restype = ctypes.c_uint16
        assert fn() == int(expected), name
    native = {}
    entries = {}
    for target, (abi, _) in TARGETS.items():
        library = source / f'target/{target}/release/libnostr_sdk_ffi.so'
        symbols = subprocess.check_output([str(toolbin / 'llvm-nm'), '-D', '--defined-only', str(library)], text=True)
        for name, _ in checks:
            assert name in symbols, f'Missing export for {abi}: {name}'
        raw = library.read_bytes()
        for personal in [str(Path.home()).encode(), str(source).encode()]:
            assert personal not in raw, f'Unmapped build path in {abi}'
        name = f'jni/{abi}/libnostr_sdk_ffi.so'
        entries[name] = raw
        native[abi] = {'sha256': sha(library), 'target': target}
    with zipfile.ZipFile(aar) as original:
        original_natives = {n for n in original.namelist() if n.endswith('/libnostr_sdk_ffi.so')}
        assert original_natives == set(entries)
        for name in original.namelist():
            if not name.endswith('/') and name not in entries:
                entries[name] = original.read(name)
    entries['AndroidManifest.xml'] = entries['AndroidManifest.xml'].replace(b'minSdkVersion="21"', b'minSdkVersion="26"')
    artifact = VENDOR / PROVENANCE['localArtifact']
    with zipfile.ZipFile(artifact, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as output:
        for name in sorted(entries):
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            output.writestr(info, entries[name], compresslevel=9)
    (VENDOR / 'SHA256SUMS').write_text(f'{sha(artifact)}  {artifact.name}\n')
    (VENDOR / 'native-build.json').write_text(json.dumps({
        'artifact': artifact.name, 'sha256': sha(artifact), 'nativeLibraries': native,
        'uniffiContract': expected_contract, 'apiChecksumsVerified': len(checks),
        'cargoLockSha256': sha(source / 'Cargo.lock'), 'upstreamCommit': PROVENANCE['upstreamCommit'],
        'rustToolchain': PROVENANCE['rustToolchain'], 'androidNdk': PROVENANCE['androidNdk']
    }, indent=2) + '\n')
    print(f'Packaged {artifact.name}; {len(checks)} ABI checks passed; four architectures verified.')


if __name__ == '__main__':
    main()
