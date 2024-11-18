#!/bin/sh

echo "Compute the current API version..."

repository_name=$1
version=$2
is_snapshot=$3

if [ "$is_snapshot" = true ]
then
  version="$version-SNAPSHOT"
fi

echo "Computed current API version: $version"

echo "Clone $repository_name..."
git clone https://github.com/eclipse-keypop/$repository_name.git

cd $repository_name

echo "Checkout gh-pages branch..."
git checkout -f gh-pages

echo "Delete existing SNAPSHOT directory..."
rm -rf *-SNAPSHOT

echo "Create target directory $version..."
mkdir $version

echo "Copy javadoc files..."
cp -rf ../build/docs/javadoc/* $version/

# Create latest-stable symlink if not a SNAPSHOT version
if [ "$is_snapshot" = false ]; then
    echo "Creating latest-stable symlink..."
    rm -rf latest-stable
    ln -s $version latest-stable

    echo "Creating robots.txt..."
    cat > robots.txt << EOF
User-agent: *
Allow: /
Allow: /latest-stable/
Disallow: /*/[0-9]*/
EOF
fi

echo "Update versions list..."
echo "| Version | Documents |" > list_versions.md
echo "|:---:|---|" >> list_versions.md

# Get the list of directories sorted by version number
sorted_dirs=$(ls -d [0-9]*/ | cut -f1 -d'/' | sort -Vr)

# Add latest-stable entry if it exists
if [ -L "latest-stable" ]; then
    latest_stable=$(ls -d [0-9]*/ | grep -v SNAPSHOT | cut -f1 -d'/' | sort -Vr | head -n1)
    if [ ! -z "$latest_stable" ]; then
        echo "| latest-stable ($latest_stable) | [API documentation](latest-stable) |" >> list_versions.md
    fi
fi

# Loop through each sorted directory
for directory in $sorted_dirs
do
  echo "| $directory | [API documentation]($directory) |" >> list_versions.md
done

echo "Computed all versions:"
cat list_versions.md

cd ..

echo "Local docs update finished."