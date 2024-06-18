import copy
import glob
import json
import random

SAMPLE_DIR = '../data'
CHANGE_COUNT = 1


def main():
    sample_filenames = get_sample_filenames(SAMPLE_DIR)
    for filename in sample_filenames:
        sample = get_sample(filename)
        edited_sample = add_random_attribute_changes(sample, CHANGE_COUNT)
        persist_sample(filename, edited_sample)


def get_sample_filenames(sample_dir):
    print("reading samples from the directory: " + sample_dir)
    sample_files = glob.glob(sample_dir + '/*.json')
    return sample_files


def get_sample(filename):
    with open(filename, 'r') as file:
        return json.load(file)


def add_random_attribute_changes(sample, change_count):
    sample_copy = copy.deepcopy(sample)
    for i in range(change_count):
        randomly_edit_attribute(sample_copy)

    return sample_copy


def randomly_edit_attribute(sample):
    keys = [*sample['characteristics'].keys()]
    rand_index = random.randrange(0, len(keys))
    key_to_edit = keys[rand_index]
    del sample['characteristics'][key_to_edit]


def persist_sample(filename, sample):
    parts = filename.split(".json")
    new_filename = parts[0] + '.' + str(random.randrange(10000, 99999)) + '.json'
    sample = json.dumps(sample)
    f = open(new_filename, "w")
    f.write(sample)
    f.close()


if __name__ == '__main__':
    main()
