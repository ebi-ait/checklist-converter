import requests

from argparse import ArgumentParser, Namespace
from io import StringIO
import os
from os.path import join, isfile, isdir
import glob
from typing import List

def str_or_file_path(value: str) -> List:
    if isfile(value):
        return [value]
    elif isdir(value):
        return glob.glob(join(value, "*.xml"))
    raise ValueError("Input provided is not a file or an existing directory. Please provide proper input.")


def directory_exists(value) -> str:
    if isdir(value):
        return value
    raise FileNotFoundError(f"folder '{value}' does not exist. Please create and re-try.")


def parse_arguments() -> Namespace:
    parser = ArgumentParser()
    parser.add_argument('-i', '--input', type=str_or_file_path, required=True, help='input sample to be sent to ENA '
                                                                                    'for validation. A path to a file '
                                                                                    'or a directory can be used')
    parser.add_argument('-o', '--out_dir', type=directory_exists, required=True, help='Output directory. Please note, '
                                                                                      'documents will be saved as '
                                                                                      '{accession}.xml')
    parser.add_argument('-u', '--user', type=str, required=True, help='username for ENA REST service')
    parser.add_argument('-p', '--password', type=str, required=True, help='password for ENA REST service')
    arguments = parser.parse_args()
    return arguments


def create_submission_xml():
    submission_xml = """<?xml version="1.0" encoding="UTF-8"?>
<SUBMISSION>
   <ACTIONS>
      <ACTION>
         <ADD/>
      </ACTION>
   </ACTIONS>
</SUBMISSION>
    """
    return submission_xml


def submit(sub_xml_streamable, sample_xml_streamable, url, username, password):
    r = requests.post(url, files={'SUBMISSION': sub_xml_streamable, 'SAMPLE': sample_xml_streamable},
                      auth=(username, password))
    return r

def main(username, password, input_paths, output_path):
    submission_xml = create_submission_xml()
    submission_xml_streamable = StringIO(submission_xml)
    submission_url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/"

    responses = {}
    for sample_path in input_paths:
        submission_xml_streamable.seek(0)
        output_name_no_extension = sample_path.split('/')[-1][:-4]
        with open(sample_path, 'r') as sample_streamable:
            try:
                response = submit(submission_xml_streamable, sample_streamable, submission_url, username, password)
                os.makedirs(f"{join(output_path, str(response.status_code))}", exist_ok=True)
                full_output_path = f"{join(output_path, str(response.status_code), output_name_no_extension)}.xml"
                with open(full_output_path, 'w') as f:
                    f.write(response.text)
            except Exception as e:
                print(e)
                full_output_path = f"{join(output_path, 'error', output_name_no_extension)}.xml"
                with open(full_output_path, 'w') as f:
                    f.write(response.text)


if __name__ == '__main__':
    args = parse_arguments()
    main(args.user, args.password, args.input, args.out_dir)
