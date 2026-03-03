package org.example.sexpr.ast;

public sealed interface SAtom extends SNode
        permits SSymbol, SString, SNumber, SBool, SNull {}