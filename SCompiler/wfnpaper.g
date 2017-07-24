S -> L } {
	S.code = L.code;                           
}
S -> L E } {
	S.code = L.code + E.code;                  
}
S -> L Segno E ToCoda E DS.coda Coda E } {
	S.code = L.code + E0.code + E1.code + E0.code + E2.code;                
}
L -> L :| E {
	L.code = L1.code + L1.code + E.code;
}
L -> { E {
	L.code = E.code;
}
L -> L :| {
	L.code = L1.code | L1.code;
}
L -> LEND E {
	L.code = LEND.code | E.code;
}
L -> LEND {
	L.code = LEND.code;
}
E -> b {
  E.code = (b, b.beat, b.dur);
}
E -> |: E :| {
  E.code = E1.code | E1.code;                }
E -> |: E ND [ E ] {
  For n = 1 to ND.count
    E0.code = E0.code | E1.code 
            | ND.ending[n];
  E0.code = E0.code | E1.code | E2.code;     }
E -> E E {
	E0.code = E1.code | E2.code;         }
E -> E Segno E ToCoda E DS.Coda Coda E $ {
  E.code = E1.code | E2.code | E3.code | 
           E2.code | E4.code;                }

