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
git clone https://github.com/jeanpierrefortune/$repository_name.git

cd $repository_name

echo "Checkout gh-pages branch..."
git checkout -f gh-pages

echo "Delete existing SNAPSHOT directories except the current one..."
for snapshot in *-SNAPSHOT/; do
    if [ "$snapshot" != "$version/" ]; then
        echo "Removing old SNAPSHOT: $snapshot"
        rm -rf "$snapshot"
    fi
done

echo "Create target directory $version..."
mkdir $version

echo "Copy javadoc files..."
cp -rf ../build/docs/javadoc/* $version/
# Create latest-stable copy if not a SNAPSHOT version
if [ "$is_snapshot" = false ]; then
    echo "Creating latest-stable directory..."
    rm -rf latest-stable
    mkdir latest-stable
    cp -rf $version/* latest-stable/

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

# Find the latest stable version (first non-SNAPSHOT, non-RC version)
latest_stable=$(ls -d [0-9]*/ | grep -v SNAPSHOT | grep -v "\-rc" | cut -f1 -d'/' | sort -Vr | head -n1)

# Loop through each sorted directory
for directory in $sorted_dirs
do
    # If this is the stable version, write latest-stable entry first
    if [ "$directory" = "$latest_stable" ]; then
        echo "| latest-stable ($latest_stable) | [API documentation](latest-stable) |" >> list_versions.md
    fi
    echo "| $directory | [API documentation]($directory) |" >> list_versions.md
done

echo "Computed all versions:"
cat list_versions.md
cd ..
echo "Local docs update finished."