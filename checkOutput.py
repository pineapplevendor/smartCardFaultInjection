#the variables in this file need to be changed based on the test that was done
input = open('tempInput.txt', 'r+')
output = open('tempOutput.txt', 'w+')
originalOutputs = []
lineNum=0
FPGAs=21145000
for line in input:
    found=0
    lineNum=lineNum+1
    for x in originalOutputs:
        if(line==x):
            found=1
    if(found==0  and line.__contains__("Response")):
        originalOutputs.append(line)
        print lineNum
        print line
input2 = open('tempInput.txt', 'r+')
for line in input2:
    if(line.__contains__('send_APDU')):
        output.write(line)
        curNum = FPGAs.__str__();
        output.write(curNum+' \n')
        FPGAs=FPGAs+10000
    else:
        output.write(line)
