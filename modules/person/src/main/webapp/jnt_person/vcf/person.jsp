<jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
BEGIN:VCARD
VERSION:3.0
N:${currentNode.properties.lastname.string};${currentNode.properties.firstname.string}
FN:${currentNode.properties.lastname.string} ${currentNode.properties.firstname.string}
TITLE:${currentNode.properties.function.string}
ROLE:${currentNode.properties.businessUnit.string}
TEL:TYPE=WORK,VOICE:${currentNode.properties.telephone.string}
TEL;TYPE=WORK,CELL:${currentNode.properties.cellular.string}
TEL;TYPE=WORK,FAX:${currentNode.properties.fax.string}
EMAIL;TYPE=PREF,INTERNET:${currentNode.properties.email.string}
PHOTO:TYPE=HREF,INTERNET:${picture.node.url}
REV:${currentNode.properties['jcr:lastModified'].time}
END:VCARD
