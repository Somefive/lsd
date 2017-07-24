S -> { E } {
	S.code = E.code;
}

S -> { E :| } {
	C = createLoop();
	S.code = (looplb,C) + E.code + (rep,C,1);
}

S -> { E :| E } {
	C = createLoop();
	S.code = (looplb,C) + E.code + (rep,C,1) + E1.code;
}

S -> L :| E } {
	C = createLoop();
	S.code = (looplb,C) + L.code + (rep,C,1) + E.code;
}

S -> L :| } {
	C = createLoop();
	S.code = (looplb,C) + L.code + (rep,C,1);
}

L -> L :| {
	C = createLoop();
	L0.code = (looplb,C) + L1.code + (rep,C,1);
}

L -> { E :| E {
	C = createLoop();
	L.code = (looplb,C) + E.code + (rep,C,1) + E1.code;
}

E -> |: E :| {
	C = createLoop();
	E.code = (looplb,C) + E1.code + (rep,C,1);
}

E -> |: E ND [ E ] {
	E0.code = (looplb,ND.loop) + E1.code + ND.code + E2.code + (rep,ND.loop,ND.count);
}

E -> E E {
	E0.code = E1.code + E2.code;
}

E -> block {
	E.code = block.code;
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