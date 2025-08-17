from transformers import pipeline
import os
import sys
import re

def clean_filename(text):
    """Clean text to be suitable for filename"""
    # Remove invalid filename characters
    text = re.sub(r'[<>:"/\\|?*]', '', text)
    # Replace spaces and multiple spaces with underscores
    text = re.sub(r'\s+', '_', text)
    # Remove leading/trailing underscores and dots
    text = text.strip('_.')
    # Limit length to reasonable filename size
    return text[:50] if text else "summary"

# Set cache directory for models (optional - transformers will use default cache)
cache_dir = os.path.join(os.path.expanduser("~"), ".cache", "huggingface", "transformers")
os.makedirs(cache_dir, exist_ok=True)

try:
    print("Loading text summarization model (this may take a moment on first run)...")
    # The model will be cached automatically by transformers library
    summarizer = pipeline("summarization", 
                         model="Falconsai/text_summarization",
                         cache_dir=cache_dir)
    print("Model loaded successfully!")
except Exception as e:
    print(f"Error loading model: {e}")
    print("Make sure you have an internet connection for the first download.")
    sys.exit(1)

directory_path = sys.argv[1] if len(sys.argv) > 1 else "."

# Only get .txt files
all_entries = os.listdir(directory_path)
txt_files = [entry for entry in all_entries if entry.lower().endswith('.txt') and os.path.isfile(os.path.join(directory_path, entry))]

if not txt_files:
    print("No .txt files found in the directory.")
    sys.exit(0)

print(f"Found {len(txt_files)} .txt files to process:")
for file in txt_files:
    print(f"  - {file}")

for file in txt_files:
    file_path = os.path.join(directory_path, file)
    try:
        # Try different encodings
        text = None
        for encoding in ['utf-8', 'utf-8-sig', 'latin-1', 'cp1252']:
            try:
                with open(file_path, "r", encoding=encoding) as f:
                    text = f.read().strip()
                    break
            except UnicodeDecodeError:
                continue
        
        if not text:
            print(f"Skipping {file}: Empty or unreadable file")
            continue
            
        if len(text) < 10:
            print(f"Skipping {file}: File too short to summarize")
            continue
            
        # Generate summary
        summary_result = summarizer(text, max_length=15, min_length=3, do_sample=False)
        summary_text = summary_result[0]['summary_text']
        
        # Clean summary for filename
        clean_summary = clean_filename(summary_text)
        
        # Create new filename
        new_filename = f"{clean_summary}.txt"
        new_file_path = os.path.join(directory_path, new_filename)
        
        # Handle duplicate filenames
        counter = 1
        while os.path.exists(new_file_path):
            new_filename = f"{clean_summary}_{counter}.txt"
            new_file_path = os.path.join(directory_path, new_filename)
            counter += 1
        
        # Rename the file
        os.rename(file_path, new_file_path)
        print(f"Renamed '{file}' -> '{new_filename}'")
        
    except Exception as e:
        print(f"Error processing {file}: {e}")