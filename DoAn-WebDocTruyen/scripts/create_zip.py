import os
import sys
import zipfile

sys.stdout.reconfigure(encoding='utf-8')

PROJECT_DIR = r"C:\Users\Admin\Downloads\Web\DoAn-WebDocTruyen"
OUT_ZIP_PARENT = r"C:\Users\Admin\Downloads\Web\Nhom6.zip"
OUT_ZIP_LOCAL = r"C:\Users\Admin\Downloads\Web\DoAn-WebDocTruyen\Nhom6.zip"

EXCLUDE_DIRS = {'build', 'target', '__pycache__', '.git', '.idea', '.vscode'}
EXCLUDE_EXTS = {'.pyc', '.class'}

def should_exclude(rel_path):
    filename = os.path.basename(rel_path)
    # Exclude Word lock/temp files
    if filename.startswith('~$'):
        return True
    parts = rel_path.replace('\\', '/').split('/')
    for p in parts:
        if p in EXCLUDE_DIRS:
            return True
    _, ext = os.path.splitext(rel_path)
    if ext.lower() in EXCLUDE_EXTS:
        return True
    if filename == 'Nhom6.zip':
        return True
    return False

def make_zip(dest_zip):
    print(f"Creating zip archive at: {dest_zip}")
    total_files = 0
    total_uncompressed_bytes = 0

    with zipfile.ZipFile(dest_zip, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as zf:
        for root, dirs, files in os.walk(PROJECT_DIR):
            # Prune excluded directories in-place
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

            for file in files:
                full_path = os.path.join(root, file)
                rel_path = os.path.relpath(full_path, PROJECT_DIR)

                if should_exclude(rel_path):
                    continue

                # Store inside folder Nhom6/ in the zip
                arcname = os.path.join("Nhom6", rel_path)
                zf.write(full_path, arcname)
                total_files += 1
                total_uncompressed_bytes += os.path.getsize(full_path)

    zip_size_mb = os.path.getsize(dest_zip) / (1024 * 1024)
    print(f"Archive created: {total_files} files packaged.")
    print(f"Total uncompressed size: {total_uncompressed_bytes / (1024 * 1024):.2f} MB")
    print(f"Final zip file size: {zip_size_mb:.2f} MB")

if __name__ == '__main__':
    # Create in parent directory c:\Users\Admin\Downloads\Web\Nhom6.zip
    make_zip(OUT_ZIP_PARENT)
    import shutil
    shutil.copy2(OUT_ZIP_PARENT, OUT_ZIP_LOCAL)
    print(f"Copied zip to local project folder: {OUT_ZIP_LOCAL}")
