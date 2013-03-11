: mark here ;
: resolve here over - 1 + swap ! ;

: if compile ?branch mark 0 , ; immediate
: else compile branch mark 0 , swap resolve ; immediate
: then resolve ; immediate

 : const create , does> @ ;
 4 const c4
 
c4 .

 : const+10 create , does> @ 10 + ;
 3 const+10 c13
 c13 .
 