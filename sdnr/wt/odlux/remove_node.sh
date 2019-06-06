#!/bin/bash
echo "Remove node_modules and node directories"
echo "Execute in directory: $(pwd)"
rm -r $(find . -type d -name "node_modules")
rm -r $(find . -type d -name "node")
