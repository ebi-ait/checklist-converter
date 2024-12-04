import json
import glob
import random
from copy import deepcopy

from os.path import join
from argparse import ArgumentParser


ERROR_TYPES = ["invalid_pattern"]
UNIVERSAL_INVALID_PATTERN = '__XYZ_BANANA_123%$£'

"""
This part can be re-usable for the future. Just add more functions that detect types of fields and makes them invalid.
"""
def invalid_pattern(document, schema):
    characteristics = schema['properties']['characteristics']['properties']
    pattern_characteristics = dict(filter(lambda x: "pattern" in x[1].get('items', {}).get('properties', {}).get('text'), characteristics.items()))
    present_in_sample = [key for key in pattern_characteristics.keys() if key in document['characteristics']]
    random_key = ""
    if present_in_sample:
        random_key = random.choice(present_in_sample)
        if document['characteristics'][random_key][0]['text'] != UNIVERSAL_INVALID_PATTERN:
            document['characteristics'][random_key][0]['text'] = UNIVERSAL_INVALID_PATTERN
        else:
            random_key = ""
    return random_key


def parse_arguments():
    parser = ArgumentParser()
    parser.add_argument('-i', '--input-documents', type=str, required=True, help='path to the input documents.')
    parser.add_argument('-s', '--schemas', type=str, required=True, help='path to the schemas.')
    parser.add_argument('-o', '--out_dir', type=str, required=True, help='output path')
    parser.add_argument('-e', '--errors_per_sample', type=int, required=False, help="Number of errors per sample. Defaults 1.", default=1)
    arguments = parser.parse_args()
    return arguments

def load_all(folder, pattern="**/*"):
    all_docs = []
    all_docs_path = glob.glob(join(folder, pattern))
    for doc_path in all_docs_path:
        with open(doc_path, 'r') as f:
            all_docs.append(json.load(f))
    return all_docs

def load_all_schemas(folder, pattern):
    """
    Had to make a different function because the checklist accession is not encoded in the schema
    :param folder:
    :param pattern:
    :return:
    """
    all_docs = {}
    all_docs_path = glob.glob(join(folder, pattern))
    for doc_path in all_docs_path:
        with open(doc_path, 'r') as f:
            all_docs[doc_path.split('/')[-1].split("-")[0]] = json.load(f)
    return all_docs



def main(samples_folder, schemas_folder, output_folder, errors_per_sample):
    all_samples = load_all(samples_folder,"*.json")
    all_schemas = load_all_schemas(schemas_folder, "*-BSD.json")
    sample_by_checklists = {}

    report = [["document_path", "checklist", "invalid_path", "error_type"]]
    for sample in all_samples:
        if sample["characteristics"]["ENA-CHECKLIST"][0]['text'] not in sample_by_checklists:
            sample_by_checklists[sample["characteristics"]["ENA-CHECKLIST"][0]['text']] = []
        sample_by_checklists[sample["characteristics"]["ENA-CHECKLIST"][0]['text']].append(sample)

    # Error introduction happens here
    for checklist, sample_list in sample_by_checklists.items():
        for sample in sample_list:
            output_path = f'{join(output_folder, sample["accession"])}.json'
            invalid_sample = deepcopy(sample)
            for _ in range(errors_per_sample):
                # TODO add functionality to ensure error is added (original != invalid)
                error_selection = random.choice(ERROR_TYPES) # THIS SELECTS THE TYPE OF ERROR
                error_function = eval(error_selection)
                changed_key = error_function(invalid_sample, all_schemas[checklist])
                if changed_key:
                    report.append([output_path, checklist, f"characteristics.{changed_key}.0.text", error_selection])
            if invalid_sample == sample:
                continue
            with open(output_path, 'w') as f:
                json.dump(invalid_sample, f, indent=4)

    with open('report.csv', 'w') as f:
        f.write("\n".join([",".join(row) for row in report]))







if __name__ == '__main__':
    args = parse_arguments()
    main(samples_folder=args.input_documents,
         schemas_folder=args.schemas,
         output_folder=args.out_dir,
         errors_per_sample=args.errors_per_sample
         )