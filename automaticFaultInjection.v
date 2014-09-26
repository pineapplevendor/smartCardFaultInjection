module myModule(readerClockActive, C, D, V, clock, three, faultLED, rstLED, doneLED);
       
        //LED to show reset is more recent than last three active
        output reg rstLED;
       
        //the clock produced by the FPGA
        input wire clock;
        //input from extender board clock signal
        input wire D;
        //supply voltage to card
        output reg V;
        //LED indicating clock should be active
        output reg readerClockActive;
        //output of FPGA's clock signal
        output reg C;
        //LED indicating reader clock has been active for three cycles
        output reg three;
        //LED indicating fault is starting
        output reg faultLED;
        //LED indicating scan is done
        output reg doneLED;
       
        //appropriate clock cycles
        integer regClockCounter = 0;
        //the variables used to count the pulses
        integer prevOneCount = 0;
        integer numOneBlocks = 0;
        integer prevZeroCount = 0;
        integer numZeroBlocks = 0;
        integer numFPGAClocks = 0;
        //tracks if there have been three consecutive cycles that appear to be reader clock
        integer threeConsecutive = 0;
        //FPGA board cycles since threeActive went to one or most recent reset
        integer timeUp = 0;
       
        //FPGA board cycles that board has been down
        integer timeDown = 0;
        //value is 1 if clock has been active at any point since programming
        reg firstTime = 0;
        //number of times the fault injection high and low vals have been incremented
        integer incrementCount = 0;
        reg hasBeenActive = 0;
        reg prevRST = 0;
        //these values are the beginning and end of when the fault is injected
        //     -they count in FPGA cycles(each cycle is 20*10^-9 s)
        integer lowVal = 6500000;
        integer highVal =6500100;
       
        always @ (posedge clock)
        begin
                V<=1;
                //this block creates a regular clock signal when the fault is not being injected
                if(timeUp<lowVal || timeUp>highVal)
                begin
                        faultLED <= 0;
                        if((regClockCounter>=10 && regClockCounter<=14) || (regClockCounter>=23 || regClockCounter<=1))
                        begin
                                C <= 1;
                        end else
                        begin
                                C <= 0;
                        end    
                end
                //this block creates the fast clock when the fault is being injected (4 times as fast, 16 mhz)
                if(timeUp>=lowVal && timeUp<=highVal)
                begin
                        faultLED <= 1;
                        rstLED <= 0;
                        if((regClockCounter>=2 && regClockCounter<=3) || (regClockCounter>=5 && regClockCounter<=6) ||
                           (regClockCounter>=8 && regClockCounter<=9) || (regClockCounter>=12 && regClockCounter <=13) ||
                           (regClockCounter>=15 && regClockCounter<=16) || (regClockCounter>=18 && regClockCounter<=19) ||
                           (regClockCounter>=21 && regClockCounter<=22) || (regClockCounter>=24 && regClockCounter<=25))
                        begin
                                C <= 1;
                        end else
                        begin
                                C <= 0;
                        end    
                end
                //this section is used to time when the clock pulses should go
                if(regClockCounter<25)
                begin
                        regClockCounter <= regClockCounter + 1;
                end else
                begin
                        regClockCounter <= 0;
                end
                //this section is used to tell when the clock from the card reader is active
                //I need to know when it starts so I can time the fault injection
                if(D==0)
                begin
                        prevZeroCount <= prevZeroCount + 1;
                        prevOneCount <= 0;
                end
                if(D==1)
                begin
                        prevOneCount <= prevOneCount + 1;
                        prevZeroCount <= 0;
                end
                if(prevOneCount > 3)
                begin
                        prevOneCount <= 0;
                        prevZeroCount <= 0;
                        numOneBlocks <= numOneBlocks + 1;
                end
                if(prevZeroCount > 3)
                begin
                        prevZeroCount <= 0;
                        prevOneCount <= 0;
                        numZeroBlocks <= numZeroBlocks + 1;
                end
                //every 1 microsecond, this checks to see if the signals being input by the card
                //reader's clock are behaving as though that clock is active
                if(numFPGAClocks < 1000)
                begin
                        numFPGAClocks <= numFPGAClocks + 1;
                end
                if(numFPGAClocks == 1000 && (numOneBlocks < 3 || numZeroBlocks < 3))
                begin
                        readerClockActive <= 0;
                        numOneBlocks <= 0;
                        numZeroBlocks <= 0;
                        numFPGAClocks <= 0;
                        threeConsecutive <= 0;
                       
                        timeDown <= timeDown + 1;
                       
                end
                if(numFPGAClocks == 1000 && numOneBlocks > 2 && numZeroBlocks > 2)
                begin
                        readerClockActive <= 1;
                        numOneBlocks <= 0;
                        numZeroBlocks <= 0;
                        numFPGAClocks <= 0;
                        threeConsecutive <= threeConsecutive + 1;
                       
                        timeDown <= 0;
                       
                end
                //if the reader clock has been active since the last reset, then the timer
                //is counting towards when the fault should be injected
                if(threeConsecutive >= 3)
                begin
                        three <= 1;
                        hasBeenActive <= 1;
                        if(firstTime==0)
                        begin
                                firstTime <= 1;
                        end
                end else
                begin
                        three <= 0;
                end
                //this is now 4 seconds
                if(timeDown >= 200000)
                begin
                        rstLED <= 1;
                        hasBeenActive <= 0;
                        timeUp <= 0;
                end
                //if rstLED goes from 0 to 1 and it isn't the first time the time for the next fault
                //to be injected is incremented by 10000 FPGA cycles
                if(prevRST==0 && rstLED==1 && firstTime!=0)
                begin
                        lowVal <= lowVal + 10000;
                        highVal <= highVal + 10000;
                        incrementCount <= incrementCount + 1;
                end
                if(rstLED==0)
                begin
                        prevRST <= 0;
                end else
                begin
                        prevRST <= 1;
                end
                //this part is to light up an LED on the FPGA when my tests are done
                // increment count can be set to however many FPGA cycles you want to test
                if(incrementCount>4536)
                begin
                        doneLED <= 1;
                end else
                begin
                        doneLED <= 0;
                end
                //if the clock has been active, add one to the timer of how long the clock has been on
                if(hasBeenActive == 1)
                begin
                        timeUp <= timeUp + 1;
                //if clock has been off, reset the time the clock has been on to 0
                end else
                begin
                        timeUp <= 0;
                end
        end
endmodule