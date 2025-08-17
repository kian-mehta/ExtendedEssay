#!/usr/bin/env python3
"""
One-time setup script to download and cache the summarization model
Run this once with: python setup_model.py
"""

from transformers import pipeline
import os

def setup_model():
    print("Downloading and caching the text summarization model...")
    print("This only needs to be done once.")
    
    try:
        # This will download and cache the model
        summarizer = pipeline("summarization", model="Falconsai/text_summarization")
        
        # Test it with a small example
        test_text = "This is a test sentence to verify the model works correctly."
        summary = summarizer(test_text, max_length=10, min_length=3, do_sample=False)
        
        print("✓ Model downloaded and cached successfully!")
        print(f"✓ Test summary: {summary[0]['summary_text']}")
        print("\nYou can now use 'autoname' command anywhere!")
        
    except Exception as e:
        print(f"✗ Error setting up model: {e}")
        print("Make sure you have:")
        print("  1. Internet connection")
        print("  2. Installed: pip install transformers torch")
        return False
    
    return True

if __name__ == "__main__":
    success = setup_model()
    if not success:
        input("Press Enter to exit...")
