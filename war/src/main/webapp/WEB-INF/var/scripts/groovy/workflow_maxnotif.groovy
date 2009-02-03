
textBody = ""
htmlBody = ""
subject = "[JAHIA] Maximum notification limit reached"
textBody += """\

Jahia Workflow Mail notification.
The maximum number of emails ($limit) sent for workflow limitation has been reached.

Original subject : ${originalSubject}

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
    <h1>Jahia Workflow Mail Notification</h1>
    <p>
      The maximum number of emails sent ($limit) for workflow limitation has been reached.
    </p>
    <h2>Info</h2>
    <p>
      Original subject : ${originalSubject} <br/>
    </p>
  </body>
"""