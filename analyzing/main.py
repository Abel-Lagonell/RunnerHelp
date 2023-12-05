import os
import csv
import numpy as np

data_dir = "./data"
sd_array = []

# Iterate over each file in the data directory
for filename in os.listdir(data_dir):
    if filename.endswith(".csv"):
        file_path = os.path.join(data_dir, filename)
        
        # Read the CSV file
        with open(file_path, "r") as file:
            reader = csv.reader(file)
            data = [float(row[0]) for row in reader]
        
        # Calculate the standard deviation
        sd = np.std(data)
        
        # Append the standard deviation to the array
        sd_array.append(sd)

print(sd_array)
