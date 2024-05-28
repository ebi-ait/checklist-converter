import os
from pathlib import Path
from tqdm import tqdm
import pandas as pd
import requests


def main():
    checklists = read_checklists_from_file('checklists.txt')
    retrieve_and_save_samples(checklists)


def get_ena_xml(accession):
    r = requests.get('https://www.ebi.ac.uk/ena/browser/api/xml/' + accession + '?includeLinks=false')
    return r.text


def get_ena_json(ers_accession):
    headers = {'accept': 'application/json'}
    r = requests.get('https://www.ebi.ac.uk/ena/submit/webin-v2/sample/' + ers_accession,
                     auth=(os.getenv('ENA_USER'), os.getenv('ENA_PASSWORD')), headers=headers)
    return r.text


def create_checklist_directory(directory_name):
    Path('samples/' + directory_name).mkdir(parents=True, exist_ok=True)


def write_to_file(filename, content):
    f = open(filename, "w")
    f.write(content)
    f.close()


def read_checklists_from_file(filename):
    f = open(filename, "r")
    checklists = f.readlines()
    print('Found ' + str(len(checklists)) + ' checklists')
    return checklists


def retrieve_and_save_samples(checklists):
    accession_list = []
    for checklist in tqdm(checklists):
        r = requests.get(
            'https://www.ebi.ac.uk/biosamples/samples?filter=attr:ENA-CHECKLIST:' + checklist + '&page=1&size=100')

        create_checklist_directory(checklist.strip())
        if '_embedded' in r.json():
            for sample in r.json()['_embedded']['samples']:
                accession = sample['accession']
                sra_accession = sample['characteristics']['SRA accession'][0]['text']
                accession_list.append({"checklist": checklist.strip(), "accession": accession, "sra_accession": sra_accession})

                # write_to_file('samples/' + checklist + '/' + accession + '.xml', get_ena_xml(accession))
                # write_to_file('samples/' + checklist + '/' + accession + '.json', get_ena_json(sra_accession))

    df = pd.DataFrame(accession_list)
    df.to_csv("accessions.csv", index=False)


if __name__ == '__main__':
    main()
