#!/usr/bin/env python3
"""Remove upstream builder home paths from ELF base-version metadata only.

No executable sections or JNI/JVM interfaces are changed. Input is pinned to
JNA 5.19.1's official AAR. Output is explicitly a locally repackaged artifact.
"""
import argparse, hashlib, json, re, struct, zipfile
from pathlib import Path
EXPECTED = 'b57125cb7d16253f0d65a80f7d3a4c3664effa711b8bdbb7f87fb572ce1624ed'
ROOT = Path(__file__).resolve().parents[1]

def elf_hash(value):
    h = 0
    for c in value:
        h = (h << 4) + c
        g = h & 0xf0000000
        if g: h ^= g >> 24
        h &= ~g
    return h

def sanitize(data):
    original = data
    b = bytearray(data)
    assert b[:4] == b'\x7fELF'
    wide = b[4] == 2
    endian = '<' if b[5] == 1 else '>'
    shoff = struct.unpack_from(endian + ('Q' if wide else 'I'), b, 40 if wide else 32)[0]
    size, count, names_index = struct.unpack_from(endian + 'HHH', b, 58 if wide else 46)
    sections = [struct.unpack_from(endian + ('IIQQQQIIQQ' if wide else 'IIIIIIIIII'), b, shoff + i * size) for i in range(count)]
    patched = 0
    for section in sections:
        if section[1] != 0x6ffffffd: continue  # SHT_GNU_verdef
        strings = sections[section[6]]
        pos, end = section[4], section[4] + section[5]
        while pos < end:
            version, flags, index, num, old_hash, aux, nxt = struct.unpack_from(endian + 'HHHHIII', b, pos)
            name_offset = struct.unpack_from(endian + 'I', b, pos + aux)[0]
            begin = strings[4] + name_offset
            stop = b.index(0, begin)
            old = bytes(b[begin:stop])
            if flags & 1 and re.match(rb'/(home|Users)/[^/]+/', old):
                assert old.endswith(b'/libjnidispatch.so')
                prefix, suffix = b'/build/jna', b'/libjnidispatch.so'
                new = prefix + b'/' * (len(old) - len(prefix) - len(suffix)) + suffix
                assert len(new) == len(old)
                b[begin:stop] = new
                struct.pack_into(endian + 'I', b, pos + 8, elf_hash(new))
                patched += 1
            if not nxt: break
            pos += nxt
    for section in sections:
        if section[2] & 4:  # SHF_EXECINSTR
            start, length = section[4], section[5]
            assert b[start:start + length] == original[start:start + length]
    assert not re.search(rb'/(home|Users)/[A-Za-z][\w.-]+/', b)
    return bytes(b), patched

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('official_aar', type=Path)
    args = parser.parse_args()
    assert hashlib.sha256(args.official_aar.read_bytes()).hexdigest() == EXPECTED
    dest = ROOT / 'vendor/jna'; dest.mkdir(parents=True, exist_ok=True)
    entries, rows = {}, []
    with zipfile.ZipFile(args.official_aar) as z:
        for name in z.namelist():
            if name.endswith('/'): continue
            raw = z.read(name)
            if name.endswith('.so'):
                updated, count = sanitize(raw)
                rows.append({'entry': name, 'originalSha256': hashlib.sha256(raw).hexdigest(), 'sha256': hashlib.sha256(updated).hexdigest(), 'baseVersionNamesAnonymized': count, 'executableSectionsUnchanged': True})
                raw = updated
            entries[name] = raw
    artifact = dest / 'jna-5.19.1-notestr.1.aar'
    with zipfile.ZipFile(artifact, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        for name, raw in sorted(entries.items()):
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info, raw)
    digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
    report = {'version': '5.19.1', 'officialUrl': 'https://repo.maven.apache.org/maven2/net/java/dev/jna/jna/5.19.1/jna-5.19.1.aar', 'officialSha256': EXPECTED, 'artifact': artifact.name, 'sha256': digest, 'nativeLibraries': rows, 'scope': 'Only ELF base version name and corresponding ELF hash changed; executable sections and JVM classes unchanged.'}
    (dest / 'provenance.json').write_text(json.dumps(report, indent=2) + '\n')
    (dest / 'SHA256SUMS').write_text(digest + '  ' + artifact.name + '\n')
    print('Repacked JNA; metadata paths removed:', sum(x['baseVersionNamesAnonymized'] for x in rows))
