@echo off
set src=%cd%\src
set out=%cd%\bin
set test=%cd%\testcases

@echo Compiling
javac %src%\Yacc.java -d %out%
@echo End

@echo Testing
echo ---------------------------------------------------
@echo off & setlocal EnableDelayedExpansion

for /f "delims=" %%a in ('"dir %test% /B"') do (
    set testName=%%~a
    set testPath=%test%\!testName!

    echo !testName!
    java -cp %out% Yacc !testPath!
    echo !testName! End
	echo ---------------------------------------------------
)
pause