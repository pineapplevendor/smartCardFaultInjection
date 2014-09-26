package outputPriKey;
 
import javacard.framework.*;
import javacard.security.*;
//this applet can output a signature, public, or private key
public class OutputPriKey extends Applet
{
        private Signature aSignature;
        private KeyPair aKeyPair=null;
        //a message to sign and an array to put the signature in
        private byte[] toFive = {0x01, 0x02, 0x03, 0x04, 0x05};
        private byte[] signed = new byte[256];
        //create an instance of the signature
        private OutputPriKey()
        {
                try
                {
                        aSignature=Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
                }
                catch(ISOException exception4)
                {
                        ISOException.throwIt((short) 0x04444);
                }
        }
        //from the documentation: “registers this applet instance with the java card runtime environment”
        public static void install(byte bArray[], short bOffset, byte bLength)
        {
                try
                {
                        new OutputPriKey().register();
                }
                catch(ISOException exception5)
                {
                        ISOException.throwIt((short) 0x05555);
                }
        }
        //this block processes an APDU command
        public void process(APDU arg0) throws ISOException
        {
                //if select -AID is being run, ends here
                try
                {
                        if (selectingApplet())
                        {
                                return;
                        }
                }
                catch(ISOException exception1)
                {
                        ISOException.throwIt((short) 0x01111);
                }
                //if no key pair is already created, make one
                try
                {
                        if(aKeyPair==null)
                        {
                                aKeyPair = new KeyPair(KeyPair.ALG_RSA_CRT,KeyBuilder.LENGTH_RSA_2048);
                                aKeyPair.genKeyPair();
                        }
                }
                catch(ISOException exception2)
                {
                        ISOException.throwIt((short) 0x02222);
                }
                //creates an output buffer
                byte buffer[] = arg0.getBuffer();
                //variables to track what should be output
                byte wantPubExp=(byte)0;
                byte wantModN=(byte)0;
                byte wantP=(byte)0;
                byte wantQ=(byte)0;
                byte wantDP1=(byte)0;
                byte wantDQ1=(byte)0;
                byte wantPQ=(byte)0;
                try
                {
                        //iterate through input buffer one character at a time
                        for(short x=(short)0;x<(short)buffer.length;x++)
                        {
                                //if there is a one in the input output pubExp
                                if(buffer[x]==(byte)1)
                                {
                                        wantPubExp=(byte)1;
                                }
                                //if there is a 2 in the input output the modulus
                                if(buffer[x]==(byte)2)
                                {
                                        wantModN=(byte)1;
                                }
                                //if there is 3 in input output prime p
                                if(buffer[x]==(byte)3)
                                {
                                        wantP=(byte)1;
                                }
                                //if there is a 4 in the input output prime Q
                                if(buffer[x]==(byte)4)
                                {
                                        wantQ=(byte)1;
                                }
                                //if there is a 5 in input output d mod (p-1)
                                //d is the private exponent
                                if(buffer[x]==(byte)5)
                                {
                                        wantDP1=(byte)1;
                                }
                                //if there is a 6 in input output d mod(q-1)
                                //d is the private exponent    
                                if(buffer[x]==(byte)6)
                                {
                                        wantDQ1=(byte)1;
                                }
                                //if there is a 7 in input output q-1 mod(p)
                                if(buffer[x]==(byte)7)
                                {
                                        wantPQ=(byte)1;
                                }
                        }
                }
                catch(ISOException exception6)
                {
                        ISOException.throwIt((short) 0x06666);
                }
                //if input apdu doesn’t select another option output signature
                if(wantPubExp==(byte)0 && wantModN==(byte)0 && wantP==(byte)0 && wantQ==(byte)0 && wantDP1==(byte)0 && wantDQ1==(byte)0 && wantPQ==(byte)0)
                {
                        try
                        {
                                aSignature.init(aKeyPair.getPrivate(), Signature.MODE_SIGN);
                                short sigLen = aSignature.sign(toFive, (short)(0), (short) toFive.length, signed, (short) 0);
                                try
                                {
                                        arg0.setOutgoing();
                                        arg0.setOutgoingLength(sigLen);
                                        arg0.sendBytesLong(signed, (short) 0, sigLen);
                                }
                                catch(ISOException exception3)
                                {
                                        ISOException.throwIt((short) 0x03333);
                                }
                        }
                        catch(CryptoException anException)
                        {
                                short errorCode = anException.getReason();
                                ISOException.throwIt(errorCode);
                        }
                }
                //output public exponent
                if(wantPubExp==(byte)1)
                {
                        try
                        {
                                RSAPublicKey pubKey = (RSAPublicKey) aKeyPair.getPublic();
                                byte[] exponentArray=new byte[256];
                                short exponentLen = pubKey.getExponent(exponentArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(exponentLen);
                                arg0.sendBytesLong(exponentArray, (short)0, exponentLen);
                        }
                        catch(ISOException exception7)
                        {
                                ISOException.throwIt((short) 0x07777);
                        }
                }
                //output modulus
                if(wantModN==(byte)1)
                {
                        try
                        {
                                RSAPublicKey pubKey = (RSAPublicKey) aKeyPair.getPublic();
                                byte[] modNArray = new byte[256];
                                short modNLen = pubKey.getModulus(modNArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(modNLen);
                                arg0.sendBytesLong(modNArray, (short)0,modNLen);
                        }
                        catch(ISOException exception8)
                        {
                                ISOException.throwIt((short) 0x08888);
                        }
                }
                //output prime p
                if(wantP==(byte)1)
                {
                        try
                        {
                                RSAPrivateCrtKey priKey = (RSAPrivateCrtKey) aKeyPair.getPrivate();
                                byte[] exponentArray = new byte[256];
                                short pLen = priKey.getP(exponentArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(pLen);
                                arg0.sendBytesLong(exponentArray, (short)0, pLen);
                        }
                        catch(ISOException exception9)
                        {
                                ISOException.throwIt((short) 0x09999);
                        }
                }
                //output prime q
                if(wantQ==(byte)1)
                {
                        try
                        {
                                RSAPrivateCrtKey priKey = (RSAPrivateCrtKey) aKeyPair.getPrivate();
                                byte[] exponentArray = new byte[256];
                                short qLen = priKey.getQ(exponentArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(qLen);
                                arg0.sendBytesLong(exponentArray, (short)0, qLen);
                        }
                        catch(ISOException exception10)
                        {
                                ISOException.throwIt((short) 0x01010);
                        }
                }
                //output d mod(p-1)
                if(wantDP1==(byte)1)
                {
                        try
                        {
                                RSAPrivateCrtKey priKey = (RSAPrivateCrtKey) aKeyPair.getPrivate();
                                byte[] exponentArray = new byte[256];
                                short dp1Len = priKey.getDP1(exponentArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(dp1Len);
                                arg0.sendBytesLong(exponentArray, (short)0, dp1Len);
                        }
                        catch(ISOException exception11)
                        {
                                ISOException.throwIt((short) 0x00011);
                        }
                }
                //output d mod(q-1)
                if(wantDQ1==(byte)1)
                {
                        try
                        {
                                RSAPrivateCrtKey priKey = (RSAPrivateCrtKey) aKeyPair.getPrivate();
                                byte[] exponentArray = new byte[256];
                                short dq1Len = priKey.getDQ1(exponentArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(dq1Len);
                                arg0.sendBytesLong(exponentArray, (short)0, dq1Len);
                        }
                        catch(ISOException exception12)
                        {
                                ISOException.throwIt((short) 0x00012);
                        }
                }
                //output q^-1 mod(p)
                if(wantPQ==(byte)1)
                {
                        try
                        {
                                RSAPrivateCrtKey priKey = (RSAPrivateCrtKey) aKeyPair.getPrivate();
                                byte[] exponentArray = new byte[256];
                                short pqLen = priKey.getPQ(exponentArray, (short)0);
                                arg0.setOutgoing();
                                arg0.setOutgoingLength(pqLen);
                                arg0.sendBytesLong(exponentArray, (short)0, pqLen);
                        }
                        catch(ISOException exception13)
                        {
                                ISOException.throwIt((short) 0x00013);
                        }
                }
        }
}