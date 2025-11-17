{$MODE OBJFPC}

Unit EntrySet;
Interface 
  Uses SysUtils, EntryP, fpjson;
  
  Procedure PushEntry(NewEntry : TEntry);


  Function  FindEntry(ID : String) : TEntry;
  { Procedure SetEntry(ID : String; Entry : TEntry); }

  Function  PEntryJson : TJSONData;

Implementation

  Var
    CurIndex   : Integer = -1; { Dynamic arrays start at 0, so this must be before 0 }
    EntryArray : Array of TEntry;

  Procedure PushEntry(NewEntry : TEntry);
  Begin

    { Check stack length }
    If (CurIndex >= High(EntryArray)) Then
      If Length(EntryArray) = 0 Then 
        SetLength(EntryArray, 1)
      Else
        SetLength(EntryArray, 2 * Length(EntryArray));

    { Push NewEntry to stack }
    Inc(CurIndex);
    EntryArray[CurIndex] := NewEntry;
  End;

  Function  PEntryJson : TJSONData;
  Var 
    CEntry   : TEntry;
    I        : Integer;
    JS_Main  : TJSONArray;

  Begin
    JS_Main  := TJSONArray.Create;

    For I:=0 To CurIndex Do
      Begin
      CEntry := EntryArray[I];
      JS_Main.Add( CEntry.ToJson );
      End;

    PEntryJson := JS_Main;
  End;

  Function FindEntry(ID : String) : TEntry;
  Var I : Integer;
  Begin
    For I:= 0 to CurIndex Do
      If EntryArray[I].IDName = ID Then
        Exit(EntryArray[I]);

    For I:=0 To CurIndex Do
      WriteLn(EntryArray[I].IDName);
    Raise Exception.Create('Couldn''t find ID ' + ID );
  End;

Initialization
  SetLength(EntryArray, 0);

End.
