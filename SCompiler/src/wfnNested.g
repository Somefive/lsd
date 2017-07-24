S -> { L } {
	S.code = L.code;
}

L -> L :| {
	C = createLoop();
	L0.code = (looplb,C) + L1.code + (rep,C,1);
}

L -> L :| E {
	C = createLoop();
	L0.code = (looplb,C) + L1.code + (rep,C,1) + E.code;
}

L -> L E {
	L0.code = L1.code + E.code;
}

L -> E {
	L.code = E.code;
}

E -> |: E :| {
	C = createLoop();
	E.code = (looplb,C) + E1.code + (rep,C,1);
}

E -> |: E [ ND ]{
	B = createLoop();
	T = createLabel();
	E0.code = (looplb,B) + E1.code + (njmp,B,0,T) + ND.code + (rep,B,ND.count) + (lb,T);
}

E -> E E {
	E0.code = E1.code + E2.code;
}

E -> block {
	E.code = block.code;
}

ND -> E :| [ ND {
	B = createLoop();
	T = createLabel();
	ND.count = ND1.count + 1;
	ND.code = (looplb,B) + E.code + (njmp,B,0,T) + ND1.code + (lb,T);
}

ND -> E {
	ND.count = 0;
	ND.code = E.code;
}