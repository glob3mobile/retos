#!/usr/bin/python

import sys
import os
import shutil


def replace_in_file(path, find_str, replace_str):
    lines = open(path).readlines()
    # write out backup
    open(os.path.splitext(path)[0] + '.deleteme', 'w').writelines(lines)
    for i in range(len(lines)):
        if find_str in lines[i]:
            lines[i] = lines[i].replace(find_str, replace_str)
    open(path, 'w').writelines(lines)


# Parse args
if len(sys.argv) < 2 or sys.argv[1] == "":
    print "Usage: make_new_project [project name] [company name (no spaces or punctuation)]"
    sys.exit(0)
proj_dir = sys.argv[1]
company_name = None
if len(sys.argv) > 2 and sys.argv[2] != "":
    company_name = sys.argv[2]

if os.path.exists("../%s" % proj_dir):
    sys.stderr.write("Cannot create project. Destination directory already exists: %s" % os.path.abspath("../%s" % proj_dir))
    sys.exit(2)

# Copy from template
shutil.copytree('assets', '../%s/assets/' % proj_dir)
shutil.copytree('Src', '../%s/Src/' % proj_dir)
shutil.copytree('Projects/Android/jni', '../%s/Projects/Android/jni' % proj_dir)
shutil.copytree('Projects/Android/res', '../%s/Projects/Android/res' % proj_dir)
shutil.copytree('Projects/Android/src', '../%s/Projects/Android/src' % proj_dir)
shutil.copy2('Projects/Android/AndroidManifest.xml', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/ant.properties', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/build.xml', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/.classpath', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/.cproject', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/.project', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/custom_rules.xml', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/project.properties', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/build.bat', '../%s/Projects/Android/' % proj_dir)
shutil.copy2('Projects/Android/build.py', '../%s/Projects/Android/' % proj_dir)
if os.path.exists('build.gradle'):
    shutil.copy2('build.gradle', '../%s/' % proj_dir)
if os.path.exists('settings.gradle'):    
    shutil.copy2('settings.gradle', '../%s/' % proj_dir)

# Make all files writeable
os.chmod("../%s" % proj_dir, 0755)
for r, d, f in os.walk("../%s" % proj_dir):
    for filename in f:
        os.chmod(os.path.join(r, filename), 0755)
    for dirname in d:
        os.chmod(os.path.join(r, dirname), 0755)

# Replace string references to "VrTemplate"
replace_in_file("../%s/Projects/Android/AndroidManifest.xml" % proj_dir, "vrtemplate", proj_dir)
replace_in_file("../%s/Projects/Android/build.xml" % proj_dir, "VrTemplate", proj_dir)
replace_in_file("../%s/Projects/Android/build.bat" % proj_dir, "vrtemplate", proj_dir)
replace_in_file("../%s/Projects/Android/build.py" % proj_dir, "vrtemplate", proj_dir)
replace_in_file("../%s/Projects/Android/.project" % proj_dir, "VrTemplate", proj_dir)
replace_in_file("../%s/Projects/Android/.cproject" % proj_dir, "VrTemplate", proj_dir)
replace_in_file("../%s/Projects/Android/src/oculus/MainActivity.java" % proj_dir, "VrTemplate", proj_dir)
replace_in_file("../%s/Projects/Android/res/values/strings.xml" % proj_dir, "VR Template", proj_dir)
if os.path.exists('settings.gradle'):
    replace_in_file("../%s/settings.gradle" % proj_dir, "VrTemplate", proj_dir)

# Replace string references to "yourcompany"
if company_name is not None:
    # Prevent dex path mismatch with mixed case 'oculus'.
    if company_name == "Oculus":
        company_name = company_name.lower()
    replace_in_file("../%s/Projects/Android/AndroidManifest.xml" % proj_dir, "yourcompany", company_name)
    replace_in_file("../%s/Projects/Android/build.bat" % proj_dir, "yourcompany", company_name)
    replace_in_file("../%s/Projects/Android/build.py" % proj_dir, "yourcompany", company_name)

# Cleanup...
for root, dirs, files in os.walk("../%s" % proj_dir):
    for filename in files:
        base, ext = os.path.splitext(filename)
        if ext and ext == ".deleteme":
            os.remove(os.path.join(root, filename))

