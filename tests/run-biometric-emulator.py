"""Run destructive vault tests only on a disposable Android emulator with fingerprint 1 enrolled."""
import subprocess
import sys
import time

serial = sys.argv[1] if len(sys.argv) > 1 else 'emulator-5580'
if not serial.startswith('emulator-'):
    raise SystemExit('These tests clear the app vault and are restricted to an emulator.')
adb = ['adb', '-s', serial]
command = adb + ['shell', 'am', 'instrument', '-w', '-e', 'class',
    'fr.decentralia.notestr.BiometricVaultTest,fr.decentralia.notestr.BiometricUiTest',
    'fr.decentralia.notestr.test/androidx.test.runner.AndroidJUnitRunner']
process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1)
transcript = []
for line in process.stdout:
    print(line, end='', flush=True)
    transcript.append(line)
    if line.strip().startswith(('WAITING_FINGERPRINT_', 'WAITING_UI_')):
        # Let Android display the real CryptoObject prompt, then supply a hardware-emulated sample.
        time.sleep(1.5)
        subprocess.run(adb + ['emu', 'finger', 'touch', '1'], check=True, stdout=subprocess.DEVNULL)
status = process.wait()
if status or 'OK (2 tests)' not in ''.join(transcript):
    raise SystemExit(1)
