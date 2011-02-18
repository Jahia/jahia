// -------------------------------------------------------------------
// Feel free to add more tags
// -------------------------------------------------------------------
mySettings = {
    previewParserPath:    '',
    onShiftEnter:        {keepDefault:false, replaceWith:'\n\n'},
    markupSet: [
        {name:'Heading 1', key:'1', openWith:'= ', closeWith:' =', placeHolder:'Your title here...' },
        {name:'Heading 2', key:'2', openWith:'== ', closeWith:' ==', placeHolder:'Your title here...' },
        {name:'Heading 3', key:'3', openWith:'=== ', closeWith:' ===', placeHolder:'Your title here...' },
        {name:'Heading 4', key:'4', openWith:'==== ', closeWith:' ====', placeHolder:'Your title here...' },
        {name:'Heading 5', key:'5', openWith:'===== ', closeWith:' =====', placeHolder:'Your title here...' },
        {separator:'---------------' },
        {name:'Bold', key:'B', openWith:"**", closeWith:"**"},
        {name:'Italic', key:'I', openWith:"//", closeWith:"//"},
        {name:'Underline', key:'U', openWith:"__", closeWith:"__"},
        {name:'Stroke through', key:'S', openWith:'--', closeWith:'--'},
        {separator:'---------------' },
        {name:'Bulleted list', dropMenu: [
            {name:'First level list', openWith:'(!(* |!|*)!)'},
            {name:'Second level list', openWith:'(!(** |!|**)!)'},
            {name:'Third level list', openWith:'(!(*** |!|**)!)'}
        ]},
        {name:'Link', key:"L", openWith:"[[[![Link]!]", closeWith:']]'},
        {separator:'---------------' },
        {name:'Quotes', openWith:'(!(> |!|>)!)', placeHolder:''},
        {name:'Script', dropMenu: [
            {name:'Superscript', openWith:"^^", closeWith:"^^"},
            {name:'Subscript', openWith:",,", closeWith:",,"}
        ]}

    ]
}