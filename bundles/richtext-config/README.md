# RichText Configuration

## How it works

There is a default configuration provided by Jahia `org.jahia.bundles.richtext.config-default.yaml` which will be applied as soon as html filtering is enable on a site. 
All site specific configuration will be used in union with the 
default. That is default configuration is applied first and then site specific configuration. You can use site specific configuration to 
add allowed elements and attributes, disallow elements or attributes on allowed elements or protocols.

1. Create a configuration file in yml format with your site key
   * `org.jahia.bundles.richtext.config-yourSiteKey.yml`
2. Add configuration. Note that disallow trumps all allow configurations.
   Which means you can have a somewhat permissive default and restrict as you see fit on per-site basis.

```
htmlFiltering:
  protocols:
    - http
    - https
  attributes:
    - name: class
      pattern: "(myclass1|myclass2)"
      elements: a, p, i
    - name: dir
    - name: id
      pattern: HTML_ID
    ...
  elements:
    - name: h1, h2, ...
  disallow:
    protocols:
      ...
    attributes:
      ...
    elements:
      ...
```
3. Deploy configuration on Jahia
4. Enable html filtering on you site
5. Edit a RichText component to have the content filtered

## Todo

1. List default patterns
