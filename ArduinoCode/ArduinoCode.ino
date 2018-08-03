
bool kill = false;
bool timedout = false;
byte steeringDeg, wheelSpeed;
bool speedON, steerON;
int offf = 0;

byte out[] = {0, 0, 0};
byte outsize = 3;

byte type;
byte pin;
byte value;

unsigned long overtime = 20;
const int overtimeFix = 150;

unsigned long speedDelay = 1500 - overtimeFix;
unsigned long steerDelay = 1500 - overtimeFix;
unsigned long lastTime = 0; //timekeeping for 50 hz, 20000 us reset
unsigned long midDelay = 0;

unsigned long lastNoKill = 0;
unsigned long sinceConnect = 0;
unsigned long sinceNoKill = 0; //Input timing for kill
unsigned long lastRun = 0; //timekeeping for loop
const unsigned long timeout = 300000; //microseconds before timeout

//#define NOT_AN_INTERRUPT -1
//where 1ms is considered full left or full reverse, and 2ms is considered full forward or full right.

void setup() {
  // put your setup code here, to run once:
  //set pins to input/output
  pinMode(9, OUTPUT); //steering
  pinMode(10, OUTPUT); //speed
  pinMode(11, INPUT); //Dead man's switch
  
  //pinMode(2, INPUT); 
  
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

void sendMessage(){
  if (!timedout && outsize >= 3){
    if (Serial.availableForWrite() > 6){
      byte msg[3] = {out[0], out[1], out[2]};
      Serial.write(msg, 3); //Send the first 3 items in the out list
  
      //Move values backwards in the list for the next run
      for (byte n = 0; n < outsize-3; n++){
        out[n] = out[n+3];
      }
      outsize -= 3;
    }
  }
}


void loop() {
  lastRun = micros();

  //Read rise of signal
  if (sinceNoKill == 0 && digitalRead(11) == HIGH){
    sinceNoKill = micros();
  }

  //Read fall of signal
  if (sinceNoKill != 0 && digitalRead(11) == LOW){
    lastNoKill = micros();
    if (kill && micros()-sinceNoKill > 1800){ //Start up if un-killed
      kill = false;
      addMessage(3, 0, 1);
    }
    if (!kill && micros()-sinceNoKill < 1600){ //Check difference to find duration of input
      kill = true;
      speedDelay = 1500 - overtimeFix;
      steerDelay = 1500 - overtimeFix;
      //Send message to computer
      addMessage(4, 0, 3);
    }
    sinceNoKill = 0;
  }

  if (!kill && micros()-lastNoKill > timeout){
    kill = true;
    addMessage(4, 0, 4);
  }

  //Grab info from buffer
  if (Serial.available() > 0){
    digitalWrite(13, LOW);
    type = Serial.read();
    pin = Serial.read();
    value = Serial.read();
    sinceConnect = micros();

    if (!kill && pin == 9){
      if (value != steeringDeg){
        //steerDelay = 1.0+((double) value)/180;
        steerDelay = map(constrain(value, 0, 180), 0, 180, 1000, 2000) - overtimeFix;
      }
      steeringDeg = value;
    }
    
    if (!kill && pin == 10){
      if (value != wheelSpeed){
        //speedDelay = 1.0+((double) value)/180;
        speedDelay = map(constrain(value, 0, 180), 0, 180, 1000, 2000) - overtimeFix;
      }
      wheelSpeed = value;
    }
  
    if (kill && type == 0xFF) { //Restart if a startup signal is recieved
      speedDelay = 1500 - overtimeFix;
      steerDelay = 1500 - overtimeFix;
      kill = false;
      addMessage(3, 0, 0);
    }
    timedout = false;
  }
  
  //Start next cycle every .02 seconds
  if (micros()-lastTime >= 20000){
    
    //digitalWrite(13, HIGH);
    digitalWrite(10, HIGH);
    speedON = true;
    
    lastTime = micros();
  }

  //Turn off the signal at approximately the correct timing.
  //int timing = micros()-lastTime;
  if (speedON && (micros()-lastTime >= speedDelay)){
    digitalWrite(13, HIGH);
    int temp = overtimeFix - (micros() - lastTime - speedDelay);
    if (temp > 2){
      if (temp < overtimeFix){
        delayMicroseconds(temp);
      } else {
        delayMicroseconds(overtimeFix);
        addMessage(6, 6, 6);
      }
    }
    digitalWrite(10, LOW);
    digitalWrite(13, LOW);
    speedON = false;

    digitalWrite(9, HIGH); //Start timing for steering
    midDelay = millis();
    steerON = true;
  }
  
  if (steerON && (micros()-midDelay >= steerDelay)){
    digitalWrite(13, HIGH);
    int temp = overtimeFix - (micros() - midDelay - steerDelay);
    if (temp > 2){
      if (temp < overtimeFix){
        delayMicroseconds(temp);
      } else {
        delayMicroseconds(overtimeFix);
        addMessage(7, 6, 6);
      }
    }
    digitalWrite(9, LOW);
    digitalWrite(13, LOW);
    steerON = false;
  }
  
  if (!kill){
    digitalWrite(13, HIGH);

    //Timeout check
    if (!timedout && Serial.peek() <= 0 && (micros()-sinceConnect) > timeout){
      timedout = true;
      addMessage(5, 0, 4);
      digitalWrite(13, LOW);
    }

  } /*else {
    if (timedout){
      if (digitalRead(11) == HIGH) {
        digitalWrite(13, HIGH);
      } else {
        digitalWrite(13, LOW);
      }
    }
  }*/

  /*int temp = 50-millis()+lastRun;
  if (temp >= 0){
    delayMicroseconds(temp);
  } else {addMessage(0, 0, abs(temp));}
  */  
  sendMessage();
}

//jssc
//
