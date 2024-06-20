import requests
import csv
import xml.etree.ElementTree as ET
import json
import random


def parse_csv_to_dict(file_path):
    result_dict = {}
    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            key = row['accession']
            value = row['checklist']
            result_dict[key] = value
    return result_dict

def get_ena_checklist(checklist_id):
    url = 'https://www.ebi.ac.uk/ena/browser/api/xml/'+checklist_id

    headers = {
        'Accept': 'application/xml'
    }

    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.content

def get_biosample(biosample_id):
    url = 'https://www.ebi.ac.uk/biosamples/samples/'+biosample_id

    headers = {
        'Accept': 'application/json'
    }

    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()

def add_mandatory_element_error(checklist_xml_str, biosample_json, biosample_id):
    root = ET.fromstring(checklist_xml_str)
    #print("biosample-json:",biosample_json)
    #find mandatory fields
    mandatory_fields = []
    for field in root.findall('.//FIELD'):
        mandatory = field.find('MANDATORY')
        name = field.find('NAME')

        if mandatory is not None and name is not None and mandatory.text == 'mandatory':
            mandatory_fields.append(name.text)
    print('Mandatory fields:',mandatory_fields)

    #now remove mandatory any of the mandatory field from json randomly and write to file
    if mandatory_fields :
        element_to_remove = mandatory_fields[random.randint(0, len(mandatory_fields)-1)]
        if element_to_remove in biosample_json["characteristics"]:
            del biosample_json["characteristics"][element_to_remove]
        else:
            raise ValueError("Element" + element_to_remove +" Not present in biosample")

        with open('./data/invalid'+biosample_id+'_mandatory.json', 'w') as file:
            json.dump(biosample_json, file, indent=4)


if __name__ == '__main__':
    accession_checklist_id_dict = parse_csv_to_dict('./data/accessions.csv')
    for accession, checklist in accession_checklist_id_dict.items():
        print(accession+' <-acc,checklist-> '+checklist)
        ena_checklist_xml = get_ena_checklist(checklist)
        biosample_json = get_biosample(accession)
        add_mandatory_element_error(ena_checklist_xml, biosample_json, accession)
