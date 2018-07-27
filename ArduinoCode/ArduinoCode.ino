
bool nokill = true;
int wheelSpeed, steeringDeg;
int wheelDelay = 1500;
int steerDelay = 1500;
int normalDelay = 1000;
int sinceConnect = 0;
int sinceNokill = 0;
byte timeout = 40;
byte out[] = {0, 0, 0};
byte outsize = 6;
//#define NOT_AN_INTERRUPT -1
//where 1ms is considered full left or full reverse, and 2ms is considered full forward or full right.

void setup() {
  // put your setup code here, to run once:
  //set pins to input/output
  pinMode(9, OUTPUT); //steering
  pinMode(10, OUTPUT); //speed
  pinMode(11, INPUT); //speed getter
  
  //pinMode(2, INPUT); //Dead man's switch
  attachInterrupt(digitalPinToInterrupt(2), killReader, CHANGE);
  
  pinMode(13, OUTPUT); //testing light
  digitalWrite(13, HIGH);

  Serial.begin(57600);
  Serial.setTimeout(1000); //Default value. available for change

  addMessage(1, 2, 6);
  
}

void addMessage(byte ina, byte inb, byte inc){
  out[outsize] = ina;
  out[outsize+1] = inb;
  out[outsize+2] = inc;
  outsize += 3;
}

//A function run when a pin interrupts.
void killReader(){
  addMessage(1, 3, 7); //send test info
  sinceNokill = 0;
}

void loop() {
  // put your main code here, to run repeatedly:
  //grab input from kill switch
  
  
  if (nokill){
    //read input from computer
    
    if (Serial.peek() <= 0 && sinceConnect > timeout){
      nokill = false;
      digitalWrite(13, HIGH);
    }
    
    sinceConnect++;
    sinceNokill++;
    
    while (Serial.available() > 0){
      digitalWrite(13, LOW);
      byte type = Serial.read();
      byte pin = Serial.read();
      byte value = Serial.read();
      sinceConnect = 0;
      
      if (pin == 9){
        if (value != steeringDeg){
          //steerDelay = 1.0+((double) value)/180;
          steerDelay = map(value, 0, 180, 1000, 2000);
        }
        steeringDeg = value;
      }
      if (pin == 10){
        if (value != wheelSpeed){
          //wheelDelay = 1.0+((double) value)/180;
          wheelDelay = map(value, 0, 180, 1000, 2000);
        }
        wheelSpeed = value;
      }
      //create normalizer delay to keep hertz more consistant
      normalDelay = 4000 - wheelDelay - steerDelay;

      //sensor in: 5V to 0
    }

    noInterrupts(); //disable interrupts to assure timing
    //Steering
    digitalWrite(9, HIGH); 
    delayMicroseconds(steerDelay); //create pulse timing
    digitalWrite(9, LOW);
    if (digitalRead(2) == HIGH) addMessage(3, 3, 3);
    //Speed control
    digitalWrite(10, HIGH);
    delayMicroseconds(wheelDelay);
    digitalWrite(10, LOW);
    interrupts();
    
    delayMicroseconds(normalDelay); //normalize to ~2ms total
    digitalWrite(13, HIGH); //restart signal light
    
    while (outsize >= 3){
      byte msg[3] = {out[0], out[1], out[2]};
      Serial.write(msg, 3); //Send the first 3 items in the out list

      //Move values backwards in the list for the next run
      for (byte n = 0; n < outsize-3; n++){
        out[n] = out[n+3];
      }
      outsize -= 3;
    }



  } else {
    
    //send message to main program once
    if (sinceConnect < timeout) {
      out[0] = 0xFF;
      Serial.write(out, 3); //send killed message
      sinceConnect = timeout;
    }
    
    //discard info from computer while dead.
    while (Serial.available() > 0){
      byte type = Serial.read();
      
      if (type != -1 && sinceConnect > timeout) {
        sinceConnect = 0; //Restart if a startup signal is recieved after a timeout
        wheelDelay = 1500;
        steerDelay = 1500;
        nokill = true;
        Serial.read();
        Serial.read();
      }
    }
    
    if (sinceConnect < timeout){ //Force car to stop instantly if kill switch is flipped
      digitalWrite(13, HIGH); 
      digitalWrite(10, HIGH);
      delayMicroseconds(1500); //timing for wheels
      digitalWrite(10, LOW);
      delayMicroseconds(2500); //normalizing timing
      digitalWrite(13, LOW); //blink main light
    } else {
      /*digitalWrite(13, LOW); 
      delay(500); //create idle pulse timing
      digitalWrite(13, HIGH);
      delay(100);*/

      if (digitalRead(2) == HIGH) {
        digitalWrite(13, HIGH);
      } else {
        digitalWrite(13, LOW);
      }
    }
    
  }
  delay(16); //add up total delay to .02 seconds (20ms), leading to 50hz.
}

//jssc
//
