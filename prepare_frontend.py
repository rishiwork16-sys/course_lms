import os
import shutil

def main():
    print("=== Frontend Preparation Tool ===")
    print("This script prepares your frontend files for hosting on GoDaddy.")
    
    backend_url = input("Enter your AWS Backend URL (e.g., https://myapp.awsapprunner.com): ").strip()
    if backend_url.endswith("/"):
        backend_url = backend_url[:-1]
    
    if not backend_url.startswith("http"):
        print("Warning: URL should start with http:// or https://")
    
    source_dir = os.path.join("backend", "src", "main", "resources", "static")
    dist_dir = "frontend_dist"
    
    # Clean dist dir
    if os.path.exists(dist_dir):
        shutil.rmtree(dist_dir)
    
    print(f"Copying files from {source_dir} to {dist_dir}...")
    shutil.copytree(source_dir, dist_dir)
    
    # Update shared.js
    shared_js_path = os.path.join(dist_dir, "js", "shared.js")
    if os.path.exists(shared_js_path):
        with open(shared_js_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Replace API_BASE
        # Looking for: const API_BASE = '/api';
        new_content = content.replace("const API_BASE = '/api';", f"const API_BASE = '{backend_url}/api';")
        
        if content == new_content:
             # Try double quotes just in case
             new_content = content.replace('const API_BASE = "/api";', f'const API_BASE = "{backend_url}/api";')

        with open(shared_js_path, "w", encoding="utf-8") as f:
            f.write(new_content)
            
        print(f"Updated {shared_js_path} with new API URL.")
    else:
        print("Error: js/shared.js not found!")
    
    print("\n=== Success! ===")
    print(f"The 'frontend_dist' folder is ready.")
    print("1. Zip the CONTENTS of 'frontend_dist'.")
    print("2. Upload the zip to your GoDaddy 'public_html' folder.")
    print("3. Extract it there.")

if __name__ == "__main__":
    main()
