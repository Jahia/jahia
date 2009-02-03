
textBody = ""
htmlBody = ""
subject = "[JAHIA] Auto export job cancelled"
textBody += """\

Auto export notification.
Export on "${target}" has been cancelled because distant site was locked.


"""
htmlBody += """\
<html>
  <head>
    <style type=\"text/css\">
      h1 {
        background-color : rgb(152, 168, 192);
        color : white;
        font-size : 16px;
        padding : 5px;
        width : 100%;
      }
      h2 {
        font-size: 14px;
        padding-left : 10px;
      }
      p {
        padding-left : 15px;
      }
      ul li {
        padding-left : 15px;
      }
      body {
        width : 800px;
        margin: 0px;
        padding : 0px;
        font-family : Verdana, Arial, Helvetica, sans-serif;
        font-size: 12px;
      }
    </style>
  </head>
  <body>
    <h1>Auto export notification</h1>
    <p>
      Export on "${target}" has been cancelled because distant site was locked.
    </p>
    <h2>Info</h2>
  </body>
"""