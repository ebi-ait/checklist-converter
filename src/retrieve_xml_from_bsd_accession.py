import argparse

import requests

from os.path import join, isfile, isdir
from typing import List
from argparse import ArgumentParser
from xml.etree import ElementTree
from uuid import uuid4



def str_or_file_path(value: str) -> List:
    if isfile(value):
        with open(value, 'r') as f:
            values = f.read().splitlines()
    else:
        values = value.split(',')
    return values


def directory_exists(value):
    if isdir(value):
        return value
    raise FileNotFoundError(f"folder '{value}' does not exist. Please create and re-try.")


def parse_arguments() -> argparse.Namespace:
    parser = ArgumentParser()
    parser.add_argument('-a', '--accessions', type=str_or_file_path, required=True, help='ENA sample accession. A file '
                                                                                         'path or a commma-separated '
                                                                                         'list of values can be '
                                                                                         'provided')
    parser.add_argument('-o', '--out_dir', type=directory_exists, required=True, help='Output directory. Please note, '
                                                                                      'documents will be saved as '
                                                                                      '{accession}.xml')
    arguments = parser.parse_args()
    return arguments


def get_document(accession):
    url = f"https://www.ebi.ac.uk/ena/browser/api/xml/{accession}"
    response = requests.get(url)
    response.raise_for_status()
    return response.text


def clean_document(xml_document: str) -> str:
    """
    Remove accession identifiers from ENA XML document. They are not allowed for ENA validation.
    :param xml_document:
    :return:
    """
    root = ElementTree.XML(xml_document)
    for parent in root.iter():
        for child in list(parent):
            if child.tag == 'SAMPLE':
                del child.attrib['accession']
                child.attrib['alias'] = str(uuid4())
            if child.tag == 'IDENTIFIERS':
                parent.remove(child)
    return ElementTree.tostring(root).decode()


def write_document(output_path, content):
    with open(output_path, 'w') as f:
        f.write(content)


def main(accessions, output_path):
    documents = {}
    for accession in accessions:
        document = get_document(accession)
        validation_ready_document = clean_document(document)
        documents[accession] = validation_ready_document

    for accession, content in documents.items():
        output_file_path = join(output_path, f"{accession}.xml")
        write_document(output_file_path, content)

if __name__ == '__main__':
    args = parse_arguments()
    main(args.accessions, args.out_dir)