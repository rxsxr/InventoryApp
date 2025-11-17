
{$MODE OBJFPC}
Program Data_Gen(Input, Output);

  Uses StrUtils, 
    SysUtils, 
    Classes,
    Rng,
    EntryP, entrySet,
    TransP,
    Date,
    INIFiles,
    {Json_Write,}

    { 2025-11-16 09:01  
    |   Just learned that FCL has an actually really good JSON library
    }
    fpjson;

  Function Date2Str(D : TDate1) : String;
    Var Ys, Ms, Ds : String;
  Begin
    Ys := Format('%.4d', [D.Year]);
    Ms := Format('%.2d', [D.Month]);
    Ds := Format('%.2d', [D.Day]);
    Date2Str := Ys + '-' + Ms + '-' + Ds;
  End;

Const 
  C_INPUTFILE  : String = 'input.ini';
  C_OUTPUTFILE : String = 'output.json';

Var 
  CurINI   : TINIFile;

  Procedure ReadNext(Sec : String);
  Var 
    SecDataList : TStringList;
    NewEntry    : TEntry;
    I : Integer;
  Begin
    SecDataList := TStringList.Create;

    CurINI.ReadSectionValues(Sec, SecDataList);
    SecDataList.AddPair('idName', Sec);

    NewEntry.InitFromStrings(SecDataList);
    NewEntry.GenerateExtras;

    entrySet.PushEntry(NewEntry);

    { Add anywhere from 3 to 18 transactions }
    For I:= 0 To IRandAB(3,18) Do
      MakeTrans(NewEntry.IDName);

    SecDataList.Destroy;
  End;

  Procedure WriteObjects;
    Var 
      JS_Main : TJsonObject; 
      JS_Prod, JS_Trans : TJsonData;
  Begin
    JS_Prod  := PEntryJson;
    JS_Trans := TransJson;

    JS_Main  := TJsonObject.Create;
    JS_Main.Add('items', JS_Prod);
    JS_Main.Add('trans', JS_Trans);
    Write(JS_Main.FormatJSON);
    Flush(Output);

    JS_Main.Destroy;
  End;

Var 
  SecList  : TStringList;
  OutFile  : TextFile;

Begin
  CurINI := TINIFile.Create(C_INPUTFILE);
  SecList := TStringList.Create;

  AssignFile(OutFile, C_OUTPUTFILE);
  Rewrite(OutFile);

  CurINI.ReadSections(SecList);

  While SecList.Count > 0 Do
    ReadNext(SecList.Shift);
  
  WriteObjects;
  Flush(OutFile);
  Close(OutFile);

End.
