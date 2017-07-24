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
	S.code = L.code 
}

S -> L Fine E DC.Fine } {
	S.code = L.code + 

S -> L Segno E Fine E DS.Fine } {
	C = createLoop();
	T = createLabel();
	S.code = L.code + (looplb,C) + E0.code + (jmp,T,C,1) + E1.code + (rep,C,1) + (lb,T);
}

S -> L Segno E DS E } {
	C = createLoop();
	S.code = L.code + (looplb,C) + E0.code + (rep,C,1) + E1.code;
}

S -> L Segno E ToCoda E DS.Coda Coda E } {
	C = createLoop();
	S.code = L.code + (looplb,C) + E0.code + (jmp,T,C,1) + E1.code + (rep,C,1) + (lb,T) + E2.code;
}

L -> L :| E {
	C = createLoop();
	L0.code = (looplb,C) + L1.code + (rep,C,1) + E.code;
}

L -> { E {
	L.code = E.code;
}

L -> L :| {
	C = createLoop();
	L.code = (looplb,C) + L1.code + (rep,C,1);
}

L -> LEND E {
	T = createLabel();
	L.code = LEND.code + E.code;
}

L -> LEND {
	L.code = LEND.code;
}

LEND -> { E ND [ E ] {
	T = createLabel();
	LEND.code = (looplb,ND.loop) + E0.code + ND.code + (njmp,T,ND.loop,ND.count) + E1.code + (lb,T) + (rep,ND.loop,ND.count);
}

E -> |: E :| {
	C = createLoop();
	E.code = (looplb,C) + E1.code + (rep,C,1);
}

E -> |: E ND [ E ] {
	T = createLabel();
	E0.code = (looplb,ND.loop) + E1.code + ND.code + (njmp,T,ND.loop,ND.count) + E2.code + (lb,T) + (rep,ND.loop,ND.count);
}

E -> E E {
	E0.code = E1.code + E2.code;
}

ND -> ND [ E :| {
	T = createLabel();
	ND.loop = ND1.loop;
	ND.count = ND1.count + 1;
	ND.code = ND1.code + (njmp,T,ND1.loop,ND1.count) + E.code + (lb,T);
}

ND -> [ E :| {
	B = createLoop();
	T = createLabel();
	ND.count = 1;
	ND.loop = B;
	ND.code = (njmp,T,B,0) + E.code + (lb,T);
}

E -> block {
	E.code = block.code;
}

E -> block X {
	B = createLoop();
	E.code = (loop,B) + block.code + (rep,B,1);
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