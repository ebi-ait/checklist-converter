https://www.ebi.ac.uk/biosamples/samples?filter=attr:ENA-CHECKLIST:ERC000023
https://www.ebi.ac.uk/ena/submit/webin-v2/swagger-ui/index.html#/retrieveAPI/getSample



We had a very productive discussion yesterday. One of the most important question in this project is how we can handle versioning in GitHub. We do not have a perfect way to handle this in GitHub, and instead we have 3 ways with pros and cons. We need to select the best strategy that suits us before moving forward. 
1. Use directory structure to handle versioning
2. Use GitHub tagging system
3. Use GitHub tagging system with custom schema for tags

## Option 1: Directory structure
Intuitive and users can browse schemas using GitHub web
Can handle individual file versioning with nicer looking URL
All schema versions are stored and no use of GitHub tagging system
```
https://github.com/ebi-ait/checklist-store-tagging/blob/main/schema/{schema-name}/{version}
https://github.com/ebi-ait/checklist-store-tagging/blob/main/schema/ERC000011/0.0.1
```
[more info]()

## Option 2: Tagging with project versioning
Users can browse schemas using GitHub tagging system
Can easily get all checklist of one version 
Can not version individual schemas. Instead, project is versioned.
Individual checklist versions increment without any change
```
https://github.com/ebi-ait/checklist-store-tagging/blob/{version}/schema/{schema-name}
https://github.com/ebi-ait/checklist-store-tagging/blob/v0.1.0/schema/ERC000011.json
```
[more info]()

## Option 3: Custom name based tags
Can handle individual versioning by implementing a wrapper API
GitHub web and tagging could be confusing to users
```
https://github.com/ebi-ait/schema-store-git/blob/{schema-name}-{version}/{schema-name}
https://github.com/ebi-ait/schema-store-git/blob/bv1/b.json
```
[more info]()
