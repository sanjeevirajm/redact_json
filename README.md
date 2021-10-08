# redact_json
Redact Json - convert Json to Json schema like format

It was created for a specifi case in a project i have worked.

This file does these things:
* Replace values other than json object or json array with the data type and length (Ex: employeeId:1234 will be transformed as employeeId: Int 4)
* Replace keys with hashcode and length if the key has more than 3 digits
* If the keys are present in importantKeys map, the original values will retained. it won't be modified


Example:
input json - 
```{
  "squadName": "Super hero squad",
  "homeTown": "Metro City",
  "formed": 2016,
  "secretBase": "Super tower",
  "active": true,
  "members": [
    {
      "name": "Molecule Man",
      "age": 29,
      "secretIdentity": "Dan Jukes",
      "powers": [
        "Radiation resistance",
        "Turning tiny",
        "Radiation blast"
      ]
    },
    {
      "name": "Madame Uppercut",
      "age": 39,
      "secretIdentity": "Jane Wilson",
      "powers": [
        "Million tonne punch",
        "Damage resistance",
        "Superhuman reflexes"
      ]
    },
    {
      "name": "Eternal Flame",
      "age": 1000000,
      "secretIdentity": "Unknown",
      "powers": [
        "Immortality",
        "Heat Immunity",
        "Inferno",
        "Teleportation",
        "Interdimensional travel"
      ]
    }
  ]
}

output json - 
```{
  "squadName": "String 16",
  "homeTown": "String 10",
  "formed": "Int 4",
  "secretBase": "String 11",
  "active": true,
  "members": [
    {
      "name": "String 12",
      "age": "Int 2",
      "secretIdentity": "String 9",
      "powers": [
        "String 20",
        "String 12",
        "String 15"
      ]
    },
    {
      "name": "String 15",
      "age": "Int 2",
      "secretIdentity": "String 11",
      "powers": [
        "String 19",
        "String 17",
        "String 19"
      ]
    },
    {
      "name": "String 13",
      "age": "Int 7",
      "secretIdentity": "String 7",
      "powers": [
        "String 11",
        "String 13",
        "String 7",
        "String 13",
        "String 23"
      ]
    }
  ]
}```
