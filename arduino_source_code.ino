#include <SoftwareSerial.h>
#include <TinyGPS.h>
// #include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_HMC5883_U.h>
#include "BTS7960.h"
#define GPS_TX_PIN 6
TinyGPS gps;

const uint8_t EN1 = 8;
const uint8_t L_PWM1 = 3;
const uint8_t R_PWM1 = 4;

const uint8_t EN2 = 9;
const uint8_t L_PWM2 = 5;
const uint8_t R_PWM2 = 7;


BTS7960 motorController1(EN1, L_PWM1, R_PWM1);
BTS7960 motorController2(EN2, L_PWM2, R_PWM2);
SoftwareSerial nss(GPS_TX_PIN, 255);
Adafruit_HMC5883_Unified mag = Adafruit_HMC5883_Unified(12345);


#define UP_PIN 22
#define DOWN_PIN 23
#define LEFT_PIN 24
#define RIGHT_PIN 25
#define SPEED 255
#define GPS_STREAM_TIMEOUT 18
#define GPS_WAYPOINT_TIMEOUT 45
#define GPS_UPDATE_INTERVAL 1000
#define DEGTORAD 0.0174532925199432957f
#define RADTODEG 57.295779513082320876f
#define DECLINATION_ANGLE 0.10f
#define COMPASS_OFFSET 0.0f
#define SPEED_LOW 150

struct Geo{
  double lat;
  double lon;
};


void setup() 
{

  pinMode(UP_PIN, OUTPUT);
  pinMode(DOWN_PIN, OUTPUT);
  pinMode(LEFT_PIN, OUTPUT);
  pinMode(RIGHT_PIN, OUTPUT);
  Serial.begin(9600);

  digitalWrite(UP_PIN, LOW);
  digitalWrite(DOWN_PIN, LOW);
  digitalWrite(LEFT_PIN, LOW);
  digitalWrite(RIGHT_PIN, LOW);
    setupCompass();
}

Geo checkGPS() {
  bool newdata = false; 
  unsigned long start = millis();
  while (millis() - start < GPS_UPDATE_INTERVAL){
    if (feedgps())
      newdata = true;
  }
  if (newdata){
    return gpsdump(gps);
  }
}

bool feedgps() {
  while(nss.available()){
    if (gps.encode(nss.read()))
      return true;
  }
  return false;
}

Geo gpsdump(TinyGPS &gps){
  float flat, flon;
  unsigned long age;
  gps.f_get_position(&flat, &flon, &age);

  Geo cooler;
  cooler.lat = flat;
  cooler.lon = flon;
  Serial.println(flat);
  Serial.println(flon);

  return cooler;
}

void driveTo(struct Geo &loc, int timeout){
       Geo cooler = checkGPS();
       
      if (cooler.lat != 0 && cooler.lon != 0){
        double d = 0;
        do{
          cooler = checkGPS();
          d = geoDistance(cooler, loc);
          double t = geoBearing(cooler, loc) - geoHeading();

          drive(d, t);
          timeout -= 1;
        }while (d > 3.0 && timeout > 0);
      }
}

double geoDistance(struct Geo &a, struct Geo &b) {
  const double R = 6371000;
  double p1 = a.lat * DEGTORAD;
  double p2 = b.lat * DEGTORAD;
  double dp = (b.lat-a.lat) * DEGTORAD;
  double dl = (b.lon-a.lon) * DEGTORAD;

  double x = sin(dp/2) * sin(dp/2) + cos(p1) * cos(p2) * sin(dl/2) * sin(dl/2);
  double y = atan2(sqrt(x), sqrt(1-x));

  return R * y;
  
}

double geoHeading(){
  sensors_event_t event;
  mag.getEvent(&event);

  double heading = atan2(event.magnetic.y, event.magnetic.x);
  heading -= DECLINATION_ANGLE;
  heading -= COMPASS_OFFSET;

  if (heading > 2 * PI)
    heading -= 2 * PI;

  double headingDeg = heading * 180/M_PI;

  while (headingDeg < -180) headingDeg += 360;
  while (headingDeg > 180) headingDeg -= 360;

  return headingDeg;
}

double geoBearing(struct Geo &a, struct Geo &b){
  float y = sin(b.lon-a.lon) * cos(b.lat);
  float x = cos(a.lat)*sin(b.lat) - sin(a.lat)*cos(b.lat)*cos(b.lon-a.lon);
  return atan2(y, x) * RADTODEG;
}

void drive(int distance, float turn) {
  int fullSpeed = 230;
  int stopSpeed = 0;

  int s = fullSpeed;
  if (distance < 8){
    int wouldBe = s - stopSpeed;
    wouldBe *= distance / 8.0f;
    s = stopSpeed + wouldBe;
  }

  int autoThrottle = constrain(s, stopSpeed, fullSpeed);
  autoThrottle = 230;

  float t = turn;
  while(t < -180) t += 360;
  while(t > 180) t -= 360;

  float t_modifier = (180.0 - abs(t)) / 180.0;
  float autoSteerA = 1;
  float autoSteerB = 1;

  if (t < 0){
    autoSteerB = t_modifier;
  }else if (t > 0){
    autoSteerA = t_modifier;
  }


  int speedA = (int) (((float) autoThrottle) * autoSteerA);
  int speedB = (int) (((float) autoThrottle) * autoSteerB);

  motorController1.TurnLeft(speedA);
  motorController2.TurnLeft(speedB);
  
}

void displayCompassDetails(void)
{
  sensor_t sensor;
  mag.getSensor(&sensor);
  Serial.println("------------------------------------");
  Serial.print  ("Sensor:       "); Serial.println(sensor.name);
  Serial.print  ("Driver Ver:   "); Serial.println(sensor.version);
  Serial.print  ("Unique ID:    "); Serial.println(sensor.sensor_id);
  Serial.print  ("Max Value:    "); Serial.print(sensor.max_value); Serial.println(" uT");
  Serial.print  ("Min Value:    "); Serial.print(sensor.min_value); Serial.println(" uT");
  Serial.print  ("Resolution:   "); Serial.print(sensor.resolution); Serial.println(" uT");  
  Serial.println("------------------------------------");
  Serial.println("");
  delay(500);
}

void setupCompass() {
  if (!mag.begin()){
    Serial.println("GHINION");
    while(1);
  }
  displayCompassDetails();
}


void loop() 
{
  motorController1.Enable();
  motorController2.Enable();
  
  String a;
  while(Serial.available() > 0){
    a = Serial.readString();
    Serial.println(a);
  }

  if (a == "traking_on"){
  
    while(a != "traking_off"){
      a = Serial.readString();
      Serial.println(a);  
      String b = a.substring(0, 11);
      String c = a.substring(12, 23);
      Serial.println(b);
      Serial.println(c);
      double longitude = b.toDouble();
      double latitude = c.toDouble();
      Geo loc;
      loc.lon = longitude;
      loc.lat = latitude;
      driveTo(loc, GPS_STREAM_TIMEOUT);
    }
  }else{
      if (a == "UP"){
        motorController1.TurnLeft(SPEED);
        motorController2.TurnRight(SPEED);
      }
      if (a  == "DOWN"){
        motorController1.TurnRight(SPEED);
        motorController2.TurnLeft(SPEED);
      }
      if (a == "LEFT"){
        motorController1.TurnLeft(SPEED);
      }
      if (a == "RIGHT"){
        motorController2.TurnRight(SPEED);
      }
      if (a == "UPP"){

        motorController1.TurnLeft(SPEED_LOW);
        motorController2.TurnRight(SPEED_LOW);
        delay(100);
        motorController1.Stop();
        motorController2.Stop();
      }
      if (a == "DOWNN"){
        motorController1.TurnRight(SPEED_LOW);
        motorController2.TurnLeft(SPEED_LOW);
        delay(100);
        motorController1.Stop();
        motorController2.Stop();
      }
      if (a == "LEFTT"){
        motorController1.TurnLeft(SPEED_LOW);
        delay(100);
        motorController1.Stop();
        motorController2.Stop();
      }
      if (a == "RIGHTT"){
        motorController2.TurnRight(SPEED_LOW);
        delay(100);
        motorController1.Stop();
        motorController2.Stop();
      }
  }
}
