#ifndef ECLAIR_H_
#define ECLAIR_H_


typedef struct Message_Send{
  char* producer_name;
  char* topic;
  char* tags;
  char* keys;
  char* body;
}Message_Send_Struct;



#endif /* ECLAIR_H_ */
