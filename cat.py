import csv
import glob
import sys

files = glob.glob("*.txt")

headers = []
data = []

for file in files:
  with open(file, 'r') as infile:
    l = {}
    for line in infile:
      fields = line.strip().split(':')
      if len(fields) != 2:
        continue
      if fields[0] not in headers:
        headers.append(fields[0])
      l[fields[0]] = fields[1].strip()
    data.append(l)
      
#data.sort(key=lambda k: k['Match'])

filename = '2018_STL_cat.csv'
if len(sys.argv) > 1:
  filename = sys.argv[1]

with open(filename, 'w', newline='') as outfile:
  w = csv.DictWriter(outfile, fieldnames=headers)
  w.writeheader()
  for line in data:
    w.writerow(line)

