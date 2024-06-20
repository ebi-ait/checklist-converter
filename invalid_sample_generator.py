import requests
import csv
import xml.etree.ElementTree as ET
import json


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

    if response.status_code == 200:
        return response.content
    else:
        print(f'Failed to fetch data. HTTP Status Code: {response.status_code}')


def get_biosample(biosample_id):
    url = 'https://www.ebi.ac.uk/biosamples/samples/'+biosample_id

    headers = {
        'Accept': 'application/json'
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        return response.json()
    else:
        print(f'Failed to fetch data. HTTP Status Code: {response.status_code}')
        return None

def add_mandatory_element_error(checklist_xml_str, biosample_json):
    root = ET.fromstring(checklist_xml_str)

    #find mandatory fields
    mandatory_fields = []
    for field in root.findall('.//FIELD'):
        mandatory = field.find('MANDATORY')
        name = field.find('NAME')

        if mandatory is not None and name is not None and mandatory.text == 'mandatory':
            print(f'Found tropical_only: {name.text}')
            mandatory_fields.append(name.text)
    print('Mandatory fields:',mandatory_fields)

    #now remove mandatory any of the mandatory field from json randomly and write to file
    # Remove the specific element
    #This has to by dynamic and random , now just for testing popping it
    element_to_remove = mandatory_fields.pop()
    if element_to_remove in biosample_json["characteristics"]:
        del biosample_json["characteristics"][element_to_remove]
    else:
        raise ValueError("Element" + element_to_remove +" Not present in biosample")

    updated_json_str = json.dumps(biosample_json, indent=4)

    # write it into file
    print(updated_json_str)


if __name__ == '__main__':
    accession_checklist_id_dict = parse_csv_to_dict('./data/accessions.csv')
    for accession, checklist in accession_checklist_id_dict.items():
        print(accession+' <-acc,checklist--> '+checklist)
        ena_checklist_xml = get_ena_checklist(checklist)
        biosample_json = get_biosample(accession)
        add_mandatory_element_error(ena_checklist_xml, accession)

