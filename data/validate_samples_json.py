import json
import re
from pathlib import Path
import requests
import os
import sys
import argparse
import glob

SCHEMA_DIR = '../schema'
SAMPLE_DIR = './samples'
BIOVALIDATOR_VALIDATE_ENDPOINT = 'http://localhost:3020/validate'


def main():
    schema_filenames = get_schema_filenames(SCHEMA_DIR)
    for schema_filename in schema_filenames:
        schema_name = extract_pattern(schema_filename, 'ERC0000..')
        sample_filenames = get_sample_filenames_for_checklist(SAMPLE_DIR, schema_name)
        for sample_filename in sample_filenames:
            response = validate(schema_filename, sample_filename)
            sample_accession = extract_pattern(sample_filename, 'SAMEA[0-9]+')
            create_directory('./output/' + schema_name)
            write_to_file('./output/' + schema_name + '/' + sample_accession + '-out.json', response)


def get_schema_filenames(schema_dir):
    print("reading schema from the directory: " + schema_dir)
    schema_files = glob.glob(schema_dir + '/*-ENA.json')
    print(schema_files)
    return schema_files


def extract_pattern(filename, pattern):
    return re.findall(pattern, filename).pop(0)


def get_sample_filenames_for_checklist(sample_dir, checklist_name):
    print("reading samples from: " + sample_dir + '/' + checklist_name + '/*.json')
    checklist_files = glob.glob(sample_dir + '/' + checklist_name + '\n/*.json')  # todo \n
    print(checklist_files)
    return checklist_files


def validate(schema_filename, sample_filename):
    schema = read_file(schema_filename)
    sample = read_file(sample_filename)

    request = {
        'data': sample,
        'schema': schema
    }

    headers = {'Content-Type': 'application/json'}
    response = requests.post(BIOVALIDATOR_VALIDATE_ENDPOINT, json=request, headers=headers)
    return response.json()


def read_file(filename):
    with open(filename, 'r') as file:
        return json.load(file)


def write_to_file(filename, content):
    with open(filename, 'w') as file:
        json.dump(content, file, indent=2)
    # f = open(filename, "w")
    # f.write(content)
    # f.close()


def create_directory(directory_name):
    Path(directory_name).mkdir(parents=True, exist_ok=True)


if __name__ == "__main__":
    main()
