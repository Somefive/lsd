S -> L } {
	S.code = L.code;
}

S -> L E } {
	S.code = L.code + E.code;
}

S -> L DC E } {
	S.code = L.code + L.code + E.code;
}

S -> L ToCoda E DC.Coda Coda E } {
	S.code = L.code + E.code + L.code + E1.code;
}

S -> L Fine E DC.Fine } {
	S.code = L.code + E.code + L.code;
}

S -> L Segno E Fine E DS.Fine } {
	S.code = L.code + E0.code + E1.code + E0.code;
}

S -> L Segno E DS E } {
	S.code = L.code + E0.code + E0.code + E1.code;
}

S -> L Segno E ToCoda E DS.Coda Coda E } {
	S.code = L.code + E0.code + E1.code + E0.code + E2.code;
}

L -> L :| E {
	L.code = L1.code + L1.code + E.code;
}

L -> { E {
	L.code = E.code;
}

L -> L :| {
	L.code = L1.code + L1.code;
}

L -> LEND E {
	L.code = LEND.code + E.code;
}

L -> LEND {
	L.code = LEND.code;
}

LEND -> { E [ E :| [ E ] {
	LEND.code = E0.code + E1.code + E0.code + E2.code;
}

LEND -> { E [ E :| [ E :| [ E ] {
	LEND.code = E0.code + E1.code + E0.code + E2.code + E0.code + E3.code;
}

E -> |: E :| {
	E.code = E1.code + E1.code;
}

E -> E E {
	E.code = E1.code + E2.code;
}

E -> |: E [ E :| [ E ] {
	E.code = E1.code + E2.code + E1.code + E3.code;
}

E -> |: E [ E :| [ E :| [ E ] {
	E.code = E1.code + E2.code + E1.code + E3.code + E1.code + E4.code;
}

E -> block {
	E.code = block.code;
}

E -> block X {
	E.code = block.code + block.code;
}

E -> ts {
	E.code = ts.code;
}

E -> bo {
	E.code = bo.code;
}

E -> & {
	E.code = &.code;
}
