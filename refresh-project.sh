#!/bin/bash

echo "Refreshing project configuration..."

# Clean Maven target directory
mvn clean

# Remove IDE-specific files that might be causing issues
rm -f .classpath
rm -f .project
rm -f .settings/org.eclipse.jdt.core.prefs
rm -f .settings/org.eclipse.m2e.core.prefs

# Recompile the project
mvn compile

echo "Project refresh completed!"
echo "You may need to re-import the project in your IDE." 