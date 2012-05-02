#!/bin/bash

# This script generates a hierachical of f folder(s) d depth containing n files 

# define default values here
f=10
d=2
n=10
target="/tmp/import"
directory="/Volumes/Storage/images/documents"

while getopts f:d:n:t:s: o
do	case "$o" in
	f)	f="$OPTARG";;
	d)	d="$OPTARG";;
	n)	n="$OPTARG";;
	t)	target="$OPTARG";;
	s)	directory="$OPTARG";;
	[?])	echo >&2 "Usage: $0 [-f folders by level] [-d depth] [-n files per folder] [-s source folder] [-t target folder]"
		exit 1;;
	esac
done

declare -i f
declare -i d
declare -i t


generatePath() {
	local -i currentDepth=$1
	local line=$2
	local depthName

echo "start generatePath for currentDepth $currentDepth process $f folders generated line $line"
	local -i i=0


	case $currentDepth in 
		1) depthName="aaa";;
		2) depthName="bbb";;
		3) depthName="ccc";;
		4) depthName="ddd" ;;
		5) depthName="eee" ;;
		6) depthName="fff" ;;
		7) depthName="ggg" ;;
		8) depthName="hhh" ;;
		9) depthName="iii" ;;
	esac
	until [ $((f - i)) = "0" ];
	do
		i=i+1
		if [ $currentDepth -le $d ]; then
			generatePath $((currentDepth +1)) "$line/$depthName$i"
			
		else 
		# do the process on each created folder
		# $line/$depthName$i contains the folder name
		# create the folder
			str="$str\n$line/$depthName$i"
			mkdir -p $target$line/$depthName$i
			local -i j=0
			local -i count=`ls $directory | wc -l`
			until [ $((n - j)) = "0" ];
			do
				l=$(( ($RANDOM * (count -1) / 32767) + 1)) 
				file=`ls $directory | head -$l | tail -1`
				echo "copy '$directory/$file' to $target$line/$depthName$i" 
				cp "$directory/$file" "$target$line/$depthName$i/$j$file" 
				j=j+1				
			done

		fi; 
	done 

}



echo "Read values folders - depth - files per folder\n"
echo "taken from directory"

echo "default values are : "
echo "folders by level: $f"
echo "depth: $d"
echo "files per folder: $n"
echo "directory to look for files: $directory"
echo "target directory: $target" 

 read -p "use default values (y/n) ?" answer 
if [ "$answer" != "y" ]; then
#do something if needed

	read -p "folders ? " f
	read -p "depth ? " d
	read -p "files per folder ? " n	
	read -p "directory ? " directory
	read -p "target ? " target
fi
declare -i n

declare -i totalNbSourceFile
sourceFiles=`ls $target`
totalNbSourceFile=`ls $target | wc -l`

# create directories
str=""
generatePath 1
# Folder and files created generating zip
orig=`pwd`
cd $target
zip -r $target *
mv $target.zip ..
cd $orig
rm -rf $target
 

