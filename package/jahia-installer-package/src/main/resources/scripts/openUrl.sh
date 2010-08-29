#!/bin/sh
urlToOpen=$1
htmlview $urlToOpen || xdg-open $urlToOpen || gnome-open $urlToOpen || kfmclient openURL $urlToOpen || call-browser $urlToOpen || firefox $urlToOpen || opera $urlToOpen || konqueror $urlToOpen || epiphany $urlToOpen || mozilla $urlToOpen || netscape $urlToOpen
