------------------------------ MODULE ServerN ------------------------------
EXTENDS Naturals

CONSTANT N

ASSUME N \in Nat

VARIABLES   (* Server variables *)
            current,    (* current client accessing the resource *)
            waiting,    (* clients waiting for accessing the resource *)
            sender,     (* message read by the server *)
            (* Client variables *)
            state,     (* state of clients *)
            (* Message variables *)
            comm        (* messages to and from the server *)

vars == <<current,waiting,sender,state,comm>>
                 
-----------------------------------------------------------------------------

msg == [client: 1..N, type: {"start", "request", "answer", "release"}]

TypeInv == 
    /\ current \in 0..N
    /\ waiting \subseteq 1..N
    /\ sender \in 0..N
    /\ state \in  [1..N -> { "ask", "wait", "access"}]
    /\ comm \in SUBSET msg

Init == 
    /\ current = 0 
    /\ waiting = {}
    /\ sender = 0
    /\ state = [ c \in 1..N |-> "ask" ]
    /\ comm = {}
              
-----------------------------------------------------------------------------

RcvReleaseServer == 
    /\ \E m \in comm : sender' = m.client /\ m.type = "release"
    /\ current = sender'
    /\ IF waiting = {}
       THEN /\ current' = 0
            /\ comm' = comm \ {[ client |-> sender', type |-> "release" ]}
            /\ UNCHANGED<<waiting,state>>
       ELSE /\ \E c \in waiting : current' = c
            /\ waiting' = waiting \ {current'}
            /\ comm' = (comm    \  {[ client |-> sender', type |-> "release" ]})
                            \union {[ client |-> current', type |-> "answer" ]}
            /\ UNCHANGED<<state>>
    
RcvRequestServer == 
    /\ \E m \in comm : sender' = m.client /\ m.type = "request"
    /\ current # sender'
    /\ IF current = 0
       THEN /\ current' = sender'
            /\ comm' = (comm     \  {[ client |-> sender', type |-> "request" ]} ) 
                             \union {[ client |-> current', type |-> "answer" ]}
            /\ UNCHANGED<<waiting,state>>
       ELSE /\ comm' = comm \ {[ client |-> sender', type |-> "request" ]}
            /\ waiting' = waiting \union {sender'}
            /\ UNCHANGED<<current,state>>
 
SendRequestClient(id) == 
    /\ state[id] = "ask" 
    /\ state' = [state EXCEPT ![id] = "wait"]
    /\ comm' = comm \union {[ client |-> id, type |-> "request" ]}
    /\ UNCHANGED<<current,waiting,sender>>

ReceiveAnswerClient(id) == 
    /\ state[id] = "wait" 
    /\ \E m \in comm : m.client = id /\ m.type = "answer"
    /\ state' = [state EXCEPT ![id] = "access"]
    /\ comm' = comm \ {[ client |-> id, type |-> "answer" ]} 
    /\ UNCHANGED<<current,waiting,sender>>
           
SendReleaseClient(id) == 
    /\ state[id] = "access" 
    /\ state' = [state EXCEPT ![id] = "ask"]
    /\ comm' = comm \union {[ client |-> id, type |-> "release" ]}
    /\ UNCHANGED<<current,waiting,sender>>
  
Client(c) == SendRequestClient(c) \/ ReceiveAnswerClient(c) \/ SendReleaseClient(c)

Next ==  \E c \in 1..N :  RcvReleaseServer \/ RcvRequestServer \/ Client(c)

Fairness == 
    /\ WF_<<vars>>(RcvReleaseServer) 
    /\ WF_<<vars>>(RcvRequestServer) 
    /\ \A c \in 1..N : 
            /\ WF_<<vars>>(SendRequestClient(c)) 
            /\ WF_<<vars>>(ReceiveAnswerClient(c)) 
            /\ WF_<<vars>>(SendReleaseClient(c))

Spec == 
    /\ Init 
    /\ [][Next]_<<vars>>
    /\ Fairness

-----------------------------------------------------------------------------

StarvationFree == 
    \A c \in 1..N : (state[c] = "wait" \/ state[c] = "ask") ~> 
                state[c] = "access"

DeadlockFree == 
    \A c1 \in 1..N : (state[c1] = "wait" \/ state[c1] = "ask") ~> 
                (\E c2 \in 1..N : state[c2] = "access")

MutualExclusion == \A c1,c2 \in 1..N : 
                    state[c1] = "access" /\ state[c2] = "access" => c1 = c2

THEOREM Spec => []TypeInv

=============================================================================
