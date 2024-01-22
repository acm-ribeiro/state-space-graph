---------------------------- MODULE OneBitClock ----------------------------
VARIABLE b

Init == (b = 0) \/ (b = 1)

TypeInv == b \in {0,1}

Next == \/ b = 0 /\ b' = 1
        \/ b = 1 /\ b' = 0
           
Spec == Init /\ [][Next]_<<b>>
-----------------------------------------------------------------------------

THEOREM Spec => []TypeInv

=============================================================================
\* Modification History
\* Last modified Mon Jan 22 09:37:35 WET 2024 by acm.ribeiro
\* Created Mon Jan 22 09:36:33 WET 2024 by acm.ribeiro
