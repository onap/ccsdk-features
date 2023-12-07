#!/bin/bash

# Source definition
PATH_SOURCE_HELPSERVER="../helpserver/provider/src/main/resources/help"

# Destination
PATH_TARGET="./target"
PATH_DOC_USERDOC_DST="$PATH_TARGET/docs/guides/onap-user"

echo_sep() {
  echo "== $1 ====================================="
}

# Main

# Create PATH in dst
echo_sep "create target"
mkdir -p "$PATH_DOC_USERDOC_DST"

# Convert src MD -> dst RST
echo_sep "Converting md to rst files"
markdown_files=$(find "$PATH_SOURCE_HELPSERVER/sdnr" -type f -iname "*.md")

for file in ${markdown_files[@]}; do
  append_name=`echo "$file" | awk -F"/" '{print $(NF-1)}'`
  f="$(basename -- $file)"
  if [ "${f,,}" = "readme.md" ]; then
    rstfile="$append_name.rst"
  else
    rstfile="${f%.md}.rst"
  fi

  echo "$file to $PATH_DOC_USERDOC_DST/$rstfile"
  pandoc -s --toc -f markdown -t rst $file > "$PATH_DOC_USERDOC_DST/$rstfile"
done

# Copy PNG to dst
echo_sep "Copy PNG files from helpserver"
png_files=$(find "$PATH_SOURCE_HELPSERVER/sdnr" -type f -iname "*.png")
for file in ${png_files[@]}; do
  f="$(basename -- $file)"
  echo "$file to $f"
  cp $file "$PATH_DOC_USERDOC_DST/$f"
done

# Copy src to dst
echo_sep "Copy additional src files"
cp -r ./src/docs "$PATH_TARGET"

# Fix
echo_sep "Fix src"
# Fix abbreviations.rst
FN1="$PATH_DOC_USERDOC_DST/abbreviations.rst"
echo "Fix broken links: $FN1"
sed -i 's/ < / <h/' $FN1 
sed -i -E 's/\| http(.*) \|/\| ttp\1  \|/' $FN1
sed -i -E 's/\|  <http(.*) \|/\| <http\1  \|/' $FN1
# Fix end  

echo_sep "================Done=========================================="
