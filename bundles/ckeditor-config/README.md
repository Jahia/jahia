# CKEditor Configuration

## How it works

1. Create a configuration file in yaml format with your site key
   * `org.jahia.bundles.ckeditor.config-yourSiteKey.yaml`
2. Add configuration to allow and disallow elements and attributes
   * example below will allow elements `div` (with attributes `name` and `title`) and `span` and disallow element `p`
```
htmlFiltering:
  allow:
  - name: div
    attributes:
    - name
    - title
  - name: span
  disallow:
  - name: p
```
3. Deploy configuration on Jahia
4. Enable html filtering on you site
5. Edit a RichText component to have the content filtered

## Todo

1. Implement parser methods to return CKEditor 4 and 5 filtering config
2. Use the service to supply configuration for CkEditor component
3. Review current policy configuration and make necessary modification if needed
