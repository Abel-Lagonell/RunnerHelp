import os
import csv
import numpy as np
import matplotlib.pyplot as plt

def getName(filename):
    match filename.split('.')[0]:
        case "NoMusNoVib": return "No Music\n No Vibration"
        case "NoMusYeVib": return "No Music\n Yes Vibration"
        case "YeMusNoVib": return "Yes Music\n No Vibration"
        case "YeMusYeVib": return "Yes Music\n Yes Vibration"

def print_percentage_differences(sd_array):
    first_sd = sd_array[0][1]
    for item in sd_array[1:]:
        percentage_difference = ((item[1] - first_sd) / first_sd) * 100
        print(f"Difference from the first run ({sd_array[0][0]}): {percentage_difference:.2f}%")

data_dir = "./data"
sd_array = []

# Iterate over each file in the data directory
for filename in os.listdir(data_dir):
    if filename.endswith(".csv"):
        file_path = os.path.join(data_dir, filename)
        
        # Read the CSV file
        with open(file_path, "r") as file:
            reader = csv.reader(file)
            
            # Skip the first 2 lines
            next(reader)
            next(reader)
            
            data = [float(row[0]) for row in reader]
        
        # Calculate the standard deviation
        sd = np.std(data)
        
        # Append the standard deviation to the array
        sd_array.append((getName(filename.split('_')[1]), sd))

# Sort sd_array by the first entry
sd_array = sorted(sd_array, key=lambda x: x[0])

# Print the sorted list
for item in sd_array:
    print(item)

# Print the percentage differences
print_percentage_differences(sd_array)

# Extract x and y values from sd_array
x_values = [item[0] for item in sd_array]
y_values = [item[1] for item in sd_array]

fig = plt.figure(figsize=(6, 6))

# Create a horizontal bar chart
plt.bar(x_values, y_values)

# Add labels and title
plt.xlabel("Conditions")
plt.ylabel("Standard Deviation")
plt.title("Standard Deviation for Different Conditions")


# Save the chart as an image
plt.savefig("Visuals/bar_chart.png")

# Display the chart
plt.show()

