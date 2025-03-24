// This file contains the json2csv transformation config to create a CSV
// See: https://juanjodiaz.github.io/json2csv/#/parsers/cli
// That CSV is then submitted to Google Sheets
export default {
  fields: [
    {
      label: 'Name',
      value: row => {
        return row.nameWithOwner
      }
    },
    {
      label: 'URL',
      value: row => {
        return row.url
      }
    },
    {
      label: 'Topics',
      value: row => {
        return row.repositoryTopics.edges
          .map(e => e.node.topic.name)
          .sort()
          .join(', ')
      }
    },
    {
      label: 'Area',
      value: row => {
        if (
          row.customProperties === undefined ||
          row.customProperties.totalCount === 0
        ) {
          return ''
        }
        return row.customProperties.edges
          .filter(e => e.node.name === 'Area')
          .map(e => e.node.values)
          .sort()
          .join(', ')
      }
    },
    {
      label: 'Owner',
      value: row => {
        if (
          row.customProperties === undefined ||
          row.customProperties.totalCount === 0
        ) {
          return ''
        }
        return row.customProperties.edges
          .filter(e => e.node.name === 'Owner')
          .map(e => e.node.values)
          .sort()
          .join(', ')
      }
    },
    {
      label: 'Champion',
      value: row => {
        if (
          row.customProperties === undefined ||
          row.customProperties.totalCount === 0
        ) {
          return ''
        }
        return row.customProperties.edges
          .filter(e => e.node.name === 'Champion')
          .map(e => e.node.values)
          .sort()
          .join(', ')
      }
    },    
    {
      label: 'Description',
      value: row => {
        return row.description
      }
    },      
    {
      label: 'Archived',
      value: row => {
        return row.isArchived
      }
    },
    {
      label: 'Private',
      value: row => {
        return row.isPrivate
      }
    },    
    {
      label: 'Created At',
      value: row => {
        return row.createdAt.slice(0, 10)
      }
    },
    {
      label: 'Updated At',
      value: row => {
        return row.updatedAt.slice(0, 10)
      }
    },
    {
      label: 'Last Push',
      value: row => {
        return row.pushedAt.slice(0, 10)
      }
    },
    {
      label: 'Fetched At',
      value: row => {
        return row.fetchedAt.slice(0, 10)
      }
    }
  ]
}